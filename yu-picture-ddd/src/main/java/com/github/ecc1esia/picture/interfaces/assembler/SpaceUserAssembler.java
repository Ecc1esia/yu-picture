package com.github.ecc1esia.picture.interfaces.assembler;

import com.github.ecc1esia.picture.domain.space.entity.SpaceUser;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserAddRequest;
import com.github.ecc1esia.picture.interfaces.dto.spaceuser.SpaceUserEditRequest;
import org.springframework.beans.BeanUtils;

public class SpaceUserAssembler {

    public static SpaceUser toSpaceUserEntity(SpaceUserAddRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }

    public static SpaceUser toSpaceUserEntity(SpaceUserEditRequest request) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);
        return spaceUser;
    }
}