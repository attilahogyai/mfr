package org.mfr.mybatis.impl;

import java.util.List;

import org.mfr.mybatis.types.MPhoto;

public interface PhotoCategoryDaoMapper {
	List<MPhoto> findPhotosByCategoryId(Long id);
}
