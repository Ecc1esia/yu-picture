package com.github.ecc1esia.backend.manager.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.github.ecc1esia.backend.manager.websocket.PictureEditHandler;
import com.github.ecc1esia.backend.manager.websocket.model.PictureEditMessageTypeEnum;
import com.github.ecc1esia.backend.manager.websocket.model.PictureEditRequestMessage;
import com.github.ecc1esia.backend.manager.websocket.model.PictureEditResponseMessage;
import com.github.ecc1esia.backend.model.entity.User;
import com.github.ecc1esia.backend.service.UserService;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * 图片编辑事件处理器（消费者）
 * 该类负责处理图片编辑相关的事件，根据不同的消息类型执行相应的处理逻辑
 */
@Component
@Slf4j
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    /**
     * 处理图片编辑事件的方法
     * 根据接收到的消息类型，执行不同的处理逻辑
     *
     * @param pictureEditEvent 包含图片编辑请求消息、会话、用户和图片ID的事件对象
     * @throws Exception 如果处理过程中发生错误，抛出异常
     */
    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {
        // 从事件中提取请求消息、会话、用户和图片ID
        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        WebSocketSession session = pictureEditEvent.getSession();
        User user = pictureEditEvent.getUser();
        Long pictureId = pictureEditEvent.getPictureId();
        // 获取到消息类别
        String type = pictureEditRequestMessage.getType();

        // 将消息类型字符串转换为枚举类型
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(type);
        // 根据消息类型处理消息
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                // 处理进入编辑模式的消息
                pictureEditHandler.handleEnterEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                // 处理退出编辑模式的消息
                pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                // 处理编辑操作消息
                pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                // 如果消息类型不匹配已知类型，发送错误消息给客户端
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("消息类型错误");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
                break;
        }
    }
}
