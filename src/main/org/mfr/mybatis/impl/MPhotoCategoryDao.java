package org.mfr.mybatis.impl;

import java.util.List;

import org.mfr.mybatis.types.MPhoto;
import org.springframework.beans.factory.annotation.Autowired;

public class MPhotoCategoryDao {
	@Autowired
	PhotoCategoryDaoMapper photoCategoryDaoMapper;
	@Autowired
	CategoryDaoMapper categoryDaoMapper;
	public List<MPhoto> findPhotosByCategoryId(Long id){
		return photoCategoryDaoMapper.findPhotosByCategoryId(id);
	}
}
