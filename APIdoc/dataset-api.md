# 数据集管理模块接口文档

本模块由以下后端文件构成：

* `com.coloranalysisbackend.model.Dataset` - 数据集实体
* `com.coloranalysisbackend.model.Image` - 图像记录实体
* `com.coloranalysisbackend.repository.DatasetRepository` / `ImageRepository` - JPA 数据访问
* `com.coloranalysisbackend.service.DatasetService` - 业务逻辑（本地存储、元数据）
* `com.coloranalysisbackend.controller.DatasetController` - REST 接口

> 当前实现为**本地文件存储**，使用 `storage.base-dir` 配置的目录；
> 将来可替换为 MinIO/AWS S3 的 presigned URL 方案，`DatasetService` 只需改写 `storeImage`。

## API 列表

### 1. 创建数据集
```
POST /api/datasets
Content-Type: application/json
```
**请求体**
```json
{
  "name": "string",
  "description": "string",
  "ownerId": "user-id"
}
```
**成功响应** 200
```json
{
  "id": "<dataset-uuid>",
  "name": "...",
  "description": "...",
  "ownerId": "...",
  "storagePrefix": "<same-as-id>",
  "fileCount": 0
}
```

### 2. 列出所有数据集
```
GET /api/datasets
```
返回数据集数组。

### 3. 获取单个数据集
```
GET /api/datasets/{id}
```
返回对应数据集或 404。

### 4. 上传图片到数据集
（替代 presigned url 的本地存储实现）
```
POST /api/datasets/{id}/images/upload
Content-Type: multipart/form-data
```
表单字段 `file` 为图片文件。
成功返回存储的 `Image` 对象：
```json
{
  "id": "<image-uuid>",
  "datasetId": "...",
  "fileName": "xxx.png",
  "storageKey": "storage/<dataset-id>/xxx.png",
  "width": null,
  "height": null,
  "md5": null
}
```

### 5. 列出某数据集的图片
```
GET /api/datasets/{id}/images
```
返回本数据集在数据库中登记的所有 `Image`。

---

### 本地 vs. Presigned URL

当前模块保存上传的图片到后端所在的 `storage.base-dir` 目录。设计文档中提到的 S3 Presigned URL 以及 MinIO 只是为了让前端直接上传大文件而不用经过后端，典型流程：

1. 客户端请求 `/api/datasets/{id}/upload-url`。
2. 后端调用 S3 SDK 生成 presigned PUT URL 并返回。
3. 客户端将文件 PUT 到该 URL，S3/MinIO 存储。
4. 上传完成后客户端通知后端 `/api/datasets/{id}/images/batch` 更新元数据。

若要切换，只需在 `DatasetService` 中增加相应的 SDK 调用，且 `DatasetController` 可以保留现有的 `/images/upload` 作为 fallback。

本地实现便于初期开发和单机运行，不需要额外的对象存储服务。

---

> 文档位于仓库的 `APIdoc/dataset-api.md`，启动后 Swagger 也会自动生成同样的接口说明。