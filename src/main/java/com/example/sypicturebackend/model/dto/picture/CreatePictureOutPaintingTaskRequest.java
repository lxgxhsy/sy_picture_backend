package com.example.sypicturebackend.model.dto.picture;

import com.example.sypicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;
import java.io.Serializable;
/**
 * 创建扩图任务请求
 * @author 诺诺
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    /**
     * 图片 id
     */
    private Long pictureId;
    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;
    private static final long serialVersionUID = 1L;
}