package com.github.ecc1esia.picture.interfaces.assembler;

import com.github.ecc1esia.picture.domain.space.entity.Space;
import com.github.ecc1esia.picture.interfaces.dto.space.SpaceAddRequest;
import com.github.ecc1esia.picture.interfaces.dto.space.SpaceEditRequest;
import com.github.ecc1esia.picture.interfaces.dto.space.SpaceUpdateRequest;
import org.springframework.beans.BeanUtils;

public class SpaceAssembler {
    public static Space toSpaceEntity(SpaceAddRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceUpdateRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceEditRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

}
