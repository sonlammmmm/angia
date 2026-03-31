package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.common.enums.ConversationStatus;
import vn.dichvuangia.management.common.enums.MessageType;
import vn.dichvuangia.management.dto.request.ChatMessageRequest;
import vn.dichvuangia.management.dto.response.ChatMessageResponse;
import vn.dichvuangia.management.dto.response.ConversationResponse;
import vn.dichvuangia.management.entity.ChatMessage;
import vn.dichvuangia.management.entity.Conversation;
import vn.dichvuangia.management.entity.Customer;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.repository.ChatMessageRepository;
import vn.dichvuangia.management.repository.ConversationRepository;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service xử lý toàn bộ logic chat realtime:
 * - Quản lý conversation (tạo, đóng, assign)
 * - Gửi/nhận tin nhắn
 * - Auto-assign admin (admin ít tải nhất)
 * - Auto-reply khi không có admin online
 * - Admin takeover
 * - Tracking online admins
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final SimpMessagingTemplate messagingTemplate;

    /** Danh sách admin online (userId → username), thread-safe */
    private final Set<Long> onlineAdminIds = ConcurrentHashMap.newKeySet();

    // ── Admin Online Tracking ─────────────────────────────────────────────

    /** Đánh dấu admin online */
    public void adminOnline(Long adminId) {
        onlineAdminIds.add(adminId);
        log.info("Admin online: id={}, total online={}", adminId, onlineAdminIds.size());
    }

    /** Đánh dấu admin offline */
    public void adminOffline(Long adminId) {
        onlineAdminIds.remove(adminId);
        log.info("Admin offline: id={}, total online={}", adminId, onlineAdminIds.size());
    }

    /** Lấy danh sách admin online */
    public Set<Long> getOnlineAdminIds() {
        return Collections.unmodifiableSet(onlineAdminIds);
    }

    // ── Gửi tin nhắn (Customer) ───────────────────────────────────────────

    /**
     * Khách hàng gửi tin nhắn.
     * - Nếu chưa có conversation → tạo mới + auto-assign + auto-reply nếu cần.
     * - Nếu đã có → gửi tin nhắn vào conversation đó.
     */
    @Transactional
    public ChatMessageResponse customerSendMessage(Long customerId, ChatMessageRequest request) {
        User customer = userRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Tìm conversation hiện tại (WAITING hoặc ACTIVE) của customer
        Conversation conversation = conversationRepo
                .findByCustomerIdAndStatusIn(customerId,
                        List.of(ConversationStatus.WAITING, ConversationStatus.ACTIVE))
                .orElse(null);

        boolean isNewConversation = false;

        if (conversation == null) {
            // Tạo conversation mới
            conversation = new Conversation();
            conversation.setCustomer(customer);
            conversation.setStatus(ConversationStatus.WAITING);
            conversation = conversationRepo.save(conversation);
            isNewConversation = true;
            log.info("New conversation created: id={}, customer={}", conversation.getId(), customerId);
        }

        // Lưu tin nhắn
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(customer);
        message.setContent(request.getContent());
        message.setType(MessageType.TEXT);
        message = messageRepo.save(message);

        // Cập nhật conversation
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setUnreadCountAdmin(conversation.getUnreadCountAdmin() + 1);
        conversationRepo.save(conversation);

        ChatMessageResponse response = toMessageResponse(message);

        // Broadcast tin nhắn đến conversation topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversation.getId(), response);

        // Nếu conversation mới → notify, KHÔNG auto-assign (admin phải chủ động nhận)
        if (isNewConversation) {
            // Notify customer về conversation mới (để frontend set activeConversationId)
            messagingTemplate.convertAndSend(
                    "/topic/customer/" + customerId + "/new-conversation",
                    toConversationResponse(conversation));

            // Gửi auto-reply cho customer biết đang chờ
            sendAutoReply(conversation);

            // Notify tất cả admin về conversation mới (hiện trong tab "Chờ")
            notifyAdminsNewConversation(conversation);
        }

        // Notify admin được assign (nếu có) về tin nhắn mới
        if (conversation.getAssignedAdmin() != null) {
            notifyAdminNewMessage(conversation.getAssignedAdmin().getId(), conversation);
        }

        return response;
    }

    // ── Gửi tin nhắn (Admin) ──────────────────────────────────────────────

    /**
     * Admin gửi tin nhắn vào conversation.
     */
    @Transactional
    public ChatMessageResponse adminSendMessage(Long adminId, ChatMessageRequest request) {
        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Lưu tin nhắn
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(admin);
        message.setContent(request.getContent());
        message.setType(MessageType.TEXT);
        message = messageRepo.save(message);

        // Cập nhật conversation
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setUnreadCountCustomer(conversation.getUnreadCountCustomer() + 1);
        conversationRepo.save(conversation);

        ChatMessageResponse response = toMessageResponse(message);

        // Broadcast tin nhắn đến conversation topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversation.getId(), response);

        // Notify customer
        messagingTemplate.convertAndSend(
                "/topic/customer/" + conversation.getCustomer().getId() + "/message",
                response);

        return response;
    }

    // ── Auto-Assign Admin ─────────────────────────────────────────────────

    /**
     * Tự động gán admin có ít conversation active nhất cho conversation.
     */
    @Transactional
    public void autoAssignAdmin(Conversation conversation) {
        if (onlineAdminIds.isEmpty()) {
            log.info("No admin online for auto-assign, conversation {} stays WAITING",
                    conversation.getId());
            return;
        }

        Long bestAdminId = findLeastLoadedAdmin();
        if (bestAdminId == null) return;

        User admin = userRepo.findById(bestAdminId).orElse(null);
        if (admin == null) return;

        conversation.setAssignedAdmin(admin);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversationRepo.save(conversation);

        log.info("Auto-assigned conversation {} to admin {}", conversation.getId(), bestAdminId);

        // Notify admin mới được assign
        notifyAdminAssigned(bestAdminId, conversation);

        // Notify customer rằng đã có admin
        sendSystemMessage(conversation, "Bạn đã được kết nối với nhân viên hỗ trợ.");
    }

    /**
     * Gán tất cả conversation đang WAITING cho admin.
     */
    @Transactional
    public void assignWaitingConversations() {
        List<Conversation> waitingList = conversationRepo
                .findByStatusOrderByLastMessageAtDesc(ConversationStatus.WAITING);

        for (Conversation conv : waitingList) {
            autoAssignAdmin(conv);
        }
    }

    // ── Admin Takeover ────────────────────────────────────────────────────

    /**
     * Admin nhận (takeover) một conversation.
     */
    @Transactional
    public ConversationResponse takeoverConversation(Long adminId, Long conversationId) {
        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Long previousAdminId = conversation.getAssignedAdmin() != null
                ? conversation.getAssignedAdmin().getId() : null;

        conversation.setAssignedAdmin(admin);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversationRepo.save(conversation);

        log.info("Admin {} took over conversation {} (previous admin: {})",
                adminId, conversationId, previousAdminId);

        // Thông báo admin cũ nếu có
        if (previousAdminId != null && !previousAdminId.equals(adminId)) {
            notifyAdminAssigned(previousAdminId, conversation);
        }

        sendSystemMessage(conversation,
                "Nhân viên " + admin.getUsername() + " đã tiếp nhận cuộc hội thoại.");

        return toConversationResponse(conversation);
    }

    // ── Đóng conversation ─────────────────────────────────────────────────

    @Transactional
    public ConversationResponse closeConversation(Long conversationId, Long closedByUserId) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(LocalDateTime.now());
        conversationRepo.save(conversation);

        sendSystemMessage(conversation, "Cuộc hội thoại đã được đóng.");

        // Notify participants
        notifyAdminsConversationUpdated(conversation);

        return toConversationResponse(conversation);
    }

    // ── Mark as Seen ──────────────────────────────────────────────────────

    @Transactional
    public void markAsSeen(Long conversationId, Long readerId) {
        int updated = messageRepo.markAsSeenByConversationAndReader(conversationId, readerId);

        if (updated > 0) {
            Conversation conversation = conversationRepo.findById(conversationId).orElse(null);
            if (conversation == null) return;

            User reader = userRepo.findById(readerId).orElse(null);
            if (reader == null) return;

            // Reset unread count cho phía đọc
            boolean isCustomer = reader.getRole().getName().equals("CUSTOMER");
            if (isCustomer) {
                conversation.setUnreadCountCustomer(0);
            } else {
                conversation.setUnreadCountAdmin(0);
            }
            conversationRepo.save(conversation);

            // Broadcast seen event
            Map<String, Object> seenEvent = new HashMap<>();
            seenEvent.put("conversationId", conversationId);
            seenEvent.put("readerId", readerId);
            seenEvent.put("readerName", reader.getUsername());
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + conversationId + "/seen", seenEvent);
        }
    }

    // ── Lấy danh sách conversations ───────────────────────────────────────

    /**
     * Lấy danh sách tất cả conversation (cho admin dashboard).
     */
    public List<ConversationResponse> getAllConversations() {
        List<Conversation> conversations = conversationRepo
                .findByStatusInOrderByLastMessageAtDesc(
                        List.of(ConversationStatus.WAITING, ConversationStatus.ACTIVE));
        return conversations.stream().map(this::toConversationResponse).toList();
    }

    /**
     * Lấy conversation hiện tại của customer.
     */
    public ConversationResponse getCustomerConversation(Long customerId) {
        return conversationRepo
                .findByCustomerIdAndStatusIn(customerId,
                        List.of(ConversationStatus.WAITING, ConversationStatus.ACTIVE))
                .map(this::toConversationResponse)
                .orElse(null);
    }

    // ── Lấy tin nhắn ─────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ tin nhắn của 1 conversation (chat history).
     */
    public List<ChatMessageResponse> getMessages(Long conversationId) {
        return messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream().map(this::toMessageResponse).toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Tìm admin online có ít conversation active nhất */
    private Long findLeastLoadedAdmin() {
        if (onlineAdminIds.isEmpty()) return null;

        List<Long> adminList = new ArrayList<>(onlineAdminIds);
        List<Object[]> counts = conversationRepo.countActiveByAdminIds(adminList);

        // Tạo map adminId → count
        Map<Long, Long> loadMap = new HashMap<>();
        for (Long id : adminList) {
            loadMap.put(id, 0L);
        }
        for (Object[] row : counts) {
            loadMap.put((Long) row[0], (Long) row[1]);
        }

        // Tìm admin có load thấp nhất
        return loadMap.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(adminList.getFirst());
    }

    /** Gửi tin nhắn tự động khi không có admin online */
    private void sendAutoReply(Conversation conversation) {
        ChatMessage autoMsg = new ChatMessage();
        autoMsg.setConversation(conversation);
        autoMsg.setSender(null); // System message
        autoMsg.setContent("Cảm ơn bạn đã liên hệ! Hiện tại không có nhân viên trực tuyến. " +
                "Chúng tôi sẽ phản hồi bạn sớm nhất có thể.");
        autoMsg.setType(MessageType.SYSTEM);
        autoMsg.setSeen(false);
        autoMsg = messageRepo.save(autoMsg);

        ChatMessageResponse response = toMessageResponse(autoMsg);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversation.getId(), response);
    }

    /** Gửi tin nhắn hệ thống */
    private void sendSystemMessage(Conversation conversation, String content) {
        ChatMessage sysMsg = new ChatMessage();
        sysMsg.setConversation(conversation);
        sysMsg.setSender(null);
        sysMsg.setContent(content);
        sysMsg.setType(MessageType.SYSTEM);
        sysMsg.setSeen(false);
        sysMsg = messageRepo.save(sysMsg);

        ChatMessageResponse response = toMessageResponse(sysMsg);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversation.getId(), response);
    }

    /** Thông báo tất cả admin có conversation mới */
    private void notifyAdminsNewConversation(Conversation conversation) {
        ConversationResponse dto = toConversationResponse(conversation);
        messagingTemplate.convertAndSend("/topic/admin/conversations/new", dto);
    }

    /** Thông báo admin khi conversation được cập nhật */
    private void notifyAdminsConversationUpdated(Conversation conversation) {
        ConversationResponse dto = toConversationResponse(conversation);
        messagingTemplate.convertAndSend("/topic/admin/conversations/updated", dto);
    }

    /** Thông báo admin được assign */
    private void notifyAdminAssigned(Long adminId, Conversation conversation) {
        ConversationResponse dto = toConversationResponse(conversation);
        messagingTemplate.convertAndSend(
                "/topic/admin/" + adminId + "/assigned", dto);
    }

    /** Thông báo admin có tin nhắn mới */
    private void notifyAdminNewMessage(Long adminId, Conversation conversation) {
        ConversationResponse dto = toConversationResponse(conversation);
        messagingTemplate.convertAndSend(
                "/topic/admin/" + adminId + "/new-message", dto);
    }

    // ── Mappers ───────────────────────────────────────────────────────────

    private ConversationResponse toConversationResponse(Conversation c) {
        // Lấy fullName từ bảng Customer nếu có
        String customerFullName = customerRepo.findByCreatedBy_Id(c.getCustomer().getId())
                .map(Customer::getFullName)
                .orElse(null);
        String displayName = (customerFullName != null && !customerFullName.isBlank())
                ? customerFullName : c.getCustomer().getUsername();

        return ConversationResponse.builder()
                .id(c.getId())
                .customerId(c.getCustomer().getId())
                .customerName(displayName)
                .customerUsername(c.getCustomer().getUsername())
                .assignedAdminId(c.getAssignedAdmin() != null ? c.getAssignedAdmin().getId() : null)
                .assignedAdminName(c.getAssignedAdmin() != null ? c.getAssignedAdmin().getUsername() : null)
                .status(c.getStatus().name())
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCountAdmin(c.getUnreadCountAdmin())
                .unreadCountCustomer(c.getUnreadCountCustomer())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        String displayName = "Hệ thống";
        if (m.getSender() != null) {
            // Lấy fullName từ bảng Customer nếu là CUSTOMER
            String role = m.getSender().getRole().getName();
            if ("CUSTOMER".equals(role)) {
                displayName = customerRepo.findByCreatedBy_Id(m.getSender().getId())
                        .map(Customer::getFullName)
                        .orElse(m.getSender().getUsername());
            } else {
                displayName = m.getSender().getUsername();
            }
        }

        return ChatMessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender() != null ? m.getSender().getId() : null)
                .senderName(m.getSender() != null ? m.getSender().getUsername() : "Hệ thống")
                .senderDisplayName(displayName)
                .senderRole(m.getSender() != null ? m.getSender().getRole().getName() : "SYSTEM")
                .content(m.getContent())
                .type(m.getType().name())
                .seen(m.getSeen())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
