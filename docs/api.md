# 媒体资讯平台接口摘要

统一响应:

```json
{"code":1,"msg":"success","data":{}}
```

管理端请求头: `token: {jwt_token}`

用户端请求头: `authentication: {jwt_token}`

## 管理端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/admin/auth/login` | 管理员登录 |
| POST | `/admin/auth/logout` | 退出登录 |
| GET | `/admin/auth/info` | 当前管理员信息 |
| POST | `/admin/auth/status/{status}?id=1` | 管理员启用/禁用 |
| GET | `/admin/category/page` | 分类分页 |
| POST | `/admin/category` | 新增分类 |
| PUT | `/admin/category` | 修改分类 |
| DELETE | `/admin/category/{id}` | 删除分类 |
| POST | `/admin/category/status/{status}?id=1` | 分类启用/禁用 |
| GET | `/admin/article/page` | 稿件分页 |
| GET | `/admin/article/{id}` | 稿件详情 |
| POST | `/admin/article` | 新增稿件草稿 |
| PUT | `/admin/article` | 修改稿件 |
| POST | `/admin/article/{id}/submit` | 提交审核 |
| POST | `/admin/article/{id}/approve` | 审核通过 |
| POST | `/admin/article/{id}/reject` | 审核驳回 |
| POST | `/admin/article/{id}/publish` | 发布稿件 |
| POST | `/admin/article/{id}/offline` | 下架稿件 |
| GET | `/admin/dashboard/today` | 今日看板 |
| GET | `/admin/statistics/read` | 阅读趋势 |
| GET | `/admin/statistics/publish` | 发布趋势 |
| GET | `/admin/statistics/hot` | 热点 Top10 |
| GET | `/admin/statistics/category-distribution` | 分类分布 |
| GET | `/admin/log/page` | 操作日志分页 |

WebSocket: `/ws/audit/{adminId}`

## 用户端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/user/auth/register` | 用户注册 |
| POST | `/user/auth/login` | 用户登录 |
| GET | `/user/user/info` | 当前用户信息 |
| PUT | `/user/user/info` | 修改用户资料 |
| GET | `/user/category/list` | 启用分类列表 |
| GET | `/user/article/page` | 已发布资讯分页 |
| GET | `/user/article/{id}` | 已发布资讯详情 |
| POST | `/user/article/{id}/read` | 记录阅读行为 |
| GET | `/user/article/hot` | 热点排行 |

## 通用

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/common/upload` | OSS 文件上传，字段名 `file` |
