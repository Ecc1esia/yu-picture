package com.github.ecc1esia.backend.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.github.ecc1esia.backend.manager.auth.SpaceUserAuthManager;
import com.github.ecc1esia.backend.manager.auth.model.SpaceUserPermissionConstant;
import com.github.ecc1esia.backend.model.entity.Picture;
import com.github.ecc1esia.backend.model.entity.Space;
import com.github.ecc1esia.backend.model.entity.User;
import com.github.ecc1esia.backend.model.enums.SpaceTypeEnum;
import com.github.ecc1esia.backend.service.PictureService;
import com.github.ecc1esia.backend.service.SpaceService;
import com.github.ecc1esia.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 拦截器，建立连接前要先校验
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 建立连接前要先校验
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes 给 WebSocketSession 会话设置属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 确保请求是 Servlet 类型
        if(request instanceof ServletServerHttpRequest){
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 从请求中获取参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            // 校验图片参数是否存在
            if(StrUtil.isBlank(pictureId)){
                log.error("缺少图片参数, 拒绝握手");
                return false;
            }

            // 获取当前登录用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            // 校验用户是否已登录
            if(ObjUtil.isEmpty(loginUser)){
                log.error("用户未登录, 拒绝握手");
                return false;
            }
            // 校验用户是否有编辑当前图片的权限
            Picture picture = pictureService.getById(pictureId);
            // 校验图片是否存在
            if(ObjUtil.isEmpty(picture)){
                log.error("图片不存在, 拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            // 如果图片属于某个空间
            if(spaceId!= null){
                space = spaceService.getById(spaceId);
                // 校验空间是否存在
                if(ObjUtil.isEmpty(space)){
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }
                // 校验空间类型是否为团队空间
                if(space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()){
                    log.error("图片所在空间不是团队空间, 拒绝握手");
                    return false;
                }
            }
            // 获取用户在当前空间的权限列表
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            // 校验用户是否具有编辑图片的权限
            if(!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                log.error("用户没有编辑图片的权限, 拒绝握手");
                return false;
            }

            // 设置用户登录信息等属性到 WebSocket 会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));// 记得转换为 Long 类型
        }
        // 校验通过，允许握手
        return true;
    }

    /**
     * 握手完成后执行
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 此方法暂无实现
    }
}
