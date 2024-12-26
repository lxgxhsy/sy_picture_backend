package com.example.sypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sypicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.example.sypicturebackend.model.dto.picture.*;
import com.example.sypicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sypicturebackend.model.entity.User;
import com.example.sypicturebackend.model.enums.PictureReviewStatusEnum;
import com.example.sypicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 诺诺
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-19 15:54:34
*/
public interface PictureService extends IService<Picture> {

	/**
	 * 校验图片
	 *
	 * @param picture
	 */
	void validPicture(Picture picture);

	/**
	 * 上传图片
	 *
	 * @param inputSource
	 * @param pictureUploadRequest
	 * @param loginUser
	 * @return
	 */
	PictureVO uploadPicture(Object inputSource,
	                        PictureUploadRequest pictureUploadRequest,
	                        User loginUser);

	/**
	 * 获取图片包装类（单条）
	 *
	 * @param picture
	 * @param request
	 * @return
	 */
	PictureVO getPictureVO(Picture picture, HttpServletRequest request);

	/**
	 * 获取图片包装类（分页）
	 *
	 * @param picturePage
	 * @param request
	 * @return
	 */
	Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

	/**
	 * 获取查询对象
	 *
	 * @param pictureQueryRequest
	 * @return
	 */
	QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

	/**
	 * 图片审核
	 * @param pictureReviewRequest
	 * @param loginUser
	 */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

	/**
	 * 管理员自动过审
	 * @param picture
	 * @param loginUser
	 */
	void fillReviewParams(Picture picture, User loginUser);


	/**
	 * 管理员批量抓取图片
	 * @param pictureUploadByBatchRequest
	 * @param loginUser
	 * @return 创建成功的图片数量
	 */
	int uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);


	/**
	 * 清除图片
	 * @param oldPicture
	 */
	void clearPictureFile(Picture oldPicture);

	/**
	 * 校验空间图片的权限
	 *
	 * @param loginUser
	 * @param picture
	 */
	void checkPictureAuth(User loginUser, Picture picture);

	/**
	 * 删除图片
	 *
	 * @param pictureId
	 * @param loginUser
	 */
	void deletePicture(long pictureId, User loginUser);

	/**
	 * 编辑图片
	 *
	 * @param pictureEditRequest
	 * @param loginUser
	 */
	void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

	/**
	 * 根据颜色搜索图片
	 * @param spaceId
	 * @param picColor
	 * @param loginUser
	 * @return
	 */
	List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

	/**
	 * 批量编辑图片
	 *
	 * @param pictureEditByBatchRequest 批量编辑请求
	 * @param loginUser 登录用户
	 */
	void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);


	/**
	 * 创建扩图任务
	 *
	 * @param createPictureOutPaintingTaskRequest
	 * @param loginUser
	 */
	CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
