#!/usr/bin/env bash
# newsPlatform 全接口冒烟测试脚本
# 基础地址
BASE=http://127.0.0.1:8080
GREEN='\033[0;32m'; RED='\033[0;31m'; YEL='\033[0;33m'; NC='\033[0m'
pass=0; fail=0; lines=()

log() { lines+=("$1"); }
ok()  { pass=$((pass+1)); log "${GREEN}✅ $1${NC}"; }
bad() { fail=$((fail+1)); log "${RED}❌ $1${NC}"; }
info(){ log "${YEL}ℹ️  $1${NC}"; }

# 调接口并记录 HTTP 状态 + 响应
call() {
  local method=$1 path=$2; shift 2
  local args=()
  local body_file=""
  while [ $# -gt 0 ]; do
    case "$1" in
      -H) args+=(-H "$2"); shift 2;;
      -d) args+=(-d "$2"); shift 2;;
      -F) args+=(-F "$2"); shift 2;;
      *) shift;;
    esac
  done
  # 用临时文件拿状态码
  local out; out=$(curl -s -o /tmp/resp.txt -w "%{http_code}" -X "$method" "$BASE$path" "${args[@]}" 2>/dev/null)
  local code=$out
  local resp; resp=$(cat /tmp/resp.txt 2>/dev/null)
  echo "$code|$resp"
}

echo "=================================================="
echo " newsPlatform 全接口测试  $(date '+%H:%M:%S')"
echo "=================================================="

# ---------- 0. 健康检查 ----------
R=$(call GET "/api/v1/weixin/portal/receive?signature=x&timestamp=y&nonce=z&echostr=")
CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" != "000" ]; then ok "健康检查/服务可达 (HTTP $CODE)"; else bad "服务不可达 (HTTP $CODE)"; fi

# ---------- 文档接口 1: 微信二维码 ticket ----------
R=$(call GET "/api/v1/login/weixin_qrcode_ticket")
CODE=${R%%|*}; BODY=${R#*|}
if echo "$BODY" | grep -q "ticket"; then ok "GET /api/v1/login/weixin_qrcode_ticket -> $BODY"; else bad "GET weixin_qrcode_ticket HTTP $CODE -> $BODY"; fi

# ---------- 文档接口 2: 检查登录(轮询, 未扫码应为 null) ----------
R=$(call GET "/api/v1/login/check_login?ticket=not_exist_ticket_123")
CODE=${R%%|*}; BODY=${R#*|}
# 约定: 未扫码返回 data=null 或整个为 null; 这里只判断不报错(HTTP 200)
if [ "$CODE" = "200" ]; then ok "GET /api/v1/login/check_login (未扫码) -> $BODY"; else bad "GET check_login HTTP $CODE -> $BODY"; fi

# ---------- 文档接口 3: 公众号验签(根回调, GET) ----------
R=$(call GET "/api/v1/weixin/portal/receive?signature=abc&timestamp=123&nonce=456&echostr=echotest")
CODE=${R%%|*}; BODY=${R#*|}
info "GET /api/v1/weixin/portal/receive (验签, 错误签名应返回空/校验失败) HTTP $CODE -> '$BODY'"

# ---------- 登录拿 token ----------
# 用户端 zhangsan/123456
R=$(call POST "/user/auth/login" -H "Content-Type: application/json" -d '{"username":"zhangsan","password":"123456"}')
CODE=${R%%|*}; BODY=${R#*|}
USER_TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | head -1 | sed 's/"token":"//;s/"//')
if [ -n "$USER_TOKEN" ]; then ok "用户登录 zhangsan -> 获得 token"; else bad "用户登录失败 HTTP $CODE -> $BODY"; fi

# 管理端 admin/123456
R=$(call POST "/admin/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}')
CODE=${R%%|*}; BODY=${R#*|}
ADMIN_TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | head -1 | sed 's/"token":"//;s/"//')
if [ -n "$ADMIN_TOKEN" ]; then ok "管理员登录 admin -> 获得 token"; else bad "管理员登录失败 HTTP $CODE -> $BODY"; fi

UT="-H token:$USER_TOKEN"
AT="-H token:$ADMIN_TOKEN"

# ---------- 用户认证/资料 ----------
R=$(call POST "/user/auth/register" -H "Content-Type: application/json" -d '{"username":"test_smoke_001","password":"123456","nickname":"冒烟测试"}')
CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /user/auth/register (新用户)"; else info "POST /user/auth/register HTTP $CODE -> $BODY (可能已存在)"; fi

R=$(call GET "/user/user/info" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /user/user/info"; else bad "GET /user/user/info HTTP $CODE -> $BODY"; fi

R=$(call PUT "/user/user/info" $UT -H "Content-Type: application/json" -d '{"nickname":"张三改","avatar":"https://x.com/a.png"}'); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "PUT /user/user/info"; else bad "PUT /user/user/info HTTP $CODE -> $BODY"; fi

# ---------- 用户端文章 ----------
R=$(call GET "/user/article/page?page=1&pageSize=5" ); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /user/article/page (公开)"; else bad "GET /user/article/page HTTP $CODE -> $BODY"; fi

R=$(call GET "/user/article/5" ); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /user/article/{id} (公开详情)"; else bad "GET /user/article/5 HTTP $CODE -> $BODY"; fi

R=$(call GET "/user/article/mine/page?page=1&pageSize=5" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /user/article/mine/page"; else bad "GET /user/article/mine/page HTTP $CODE -> $BODY"; fi

R=$(call GET "/user/article/favorite/page?page=1&pageSize=5" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /user/article/favorite/page"; else bad "GET /user/article/favorite/page HTTP $CODE -> $BODY"; fi

R=$(call POST "/user/article" $UT -H "Content-Type: application/json" -d '{"title":"冒烟测试文章","summary":"测试","categoryId":1,"content":"hello","isPaid":0}'); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /user/article (创建草稿)"; else bad "POST /user/article HTTP $CODE -> $BODY"; fi

R=$(call POST "/user/article/1/like" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /user/article/{id}/like"; else bad "POST like HTTP $CODE -> $BODY"; fi

R=$(call POST "/user/article/1/dislike" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /user/article/{id}/dislike"; else bad "POST dislike HTTP $CODE -> $BODY"; fi

R=$(call POST "/user/article/1/favorite" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /user/article/{id}/favorite"; else bad "POST favorite HTTP $CODE -> $BODY"; fi

R=$(call POST "/user/article/1/read" $UT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /user/article/{id}/read"; else bad "POST read HTTP $CODE -> $BODY"; fi

# ---------- 用户端分类 ----------
R=$(call GET "/user/category/list"); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /user/category/list (公开)"; else bad "GET /user/category/list HTTP $CODE -> $BODY"; fi

# ---------- 支付(文档接口 4 的等价: 下单) ----------
R=$(call POST "/user/pay/create" $UT -H "Content-Type: application/json" -d '{"articleId":5}'); CODE=${R%%|*}; BODY=${R#*|}
ORDER_ID=$(echo "$BODY" | grep -o '"orderId":"[^"]*"' | head -1 | sed 's/"orderId":"//;s/"//')
if [ "$CODE" = "200" ]; then ok "POST /user/pay/create (articleId=5) -> orderId=$ORDER_ID"; else bad "POST /user/pay/create HTTP $CODE -> $BODY"; fi

if [ -n "$ORDER_ID" ]; then
  R=$(call GET "/user/pay/order/$ORDER_ID"); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "GET /user/pay/order/{orderId}"; else bad "GET order HTTP $CODE -> $BODY"; fi

  R=$(call POST "/user/pay/mock-success?orderId=$ORDER_ID" $UT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /user/pay/mock-success (mock 支付成功)"; else bad "POST mock-success HTTP $CODE -> $BODY"; fi

  R=$(call GET "/user/pay/purchased?articleId=5" $UT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "GET /user/pay/purchased?articleId=5"; else bad "GET purchased HTTP $CODE -> $BODY"; fi
fi

# ---------- 管理端: 文章 ----------
R=$(call GET "/admin/article/page?page=1&pageSize=5" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/article/page"; else bad "GET /admin/article/page HTTP $CODE -> $BODY"; fi

R=$(call GET "/admin/article/5" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/article/{id}"; else bad "GET /admin/article/5 HTTP $CODE -> $BODY"; fi

# 管理端创建文章
R=$(call POST "/admin/article" $AT -H "Content-Type: application/json" -d '{"title":"管理端冒烟","summary":"t","categoryId":1,"content":"c","isPaid":0}'); CODE=${R%%|*}; BODY=${R#*|}
ADM_ART_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ "$CODE" = "200" ]; then ok "POST /admin/article (新建) -> id=$ADM_ART_ID"; else bad "POST /admin/article HTTP $CODE -> $BODY"; fi

if [ -n "$ADM_ART_ID" ]; then
  R=$(call PUT "/admin/article" $AT -H "Content-Type: application/json" -d "{\"id\":$ADM_ART_ID,\"title\":\"管理端改\",\"categoryId\":1,\"content\":\"c2\"}"); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "PUT /admin/article (更新)"; else bad "PUT /admin/article HTTP $CODE -> $BODY"; fi

  R=$(call POST "/admin/article/$ADM_ART_ID/submit" $AT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /admin/article/{id}/submit"; else bad "POST submit HTTP $CODE -> $BODY"; fi

  R=$(call POST "/admin/article/$ADM_ART_ID/approve" $AT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /admin/article/{id}/approve"; else bad "POST approve HTTP $CODE -> $BODY"; fi

  R=$(call POST "/admin/article/$ADM_ART_ID/publish" $AT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /admin/article/{id}/publish"; else bad "POST publish HTTP $CODE -> $BODY"; fi

  R=$(call POST "/admin/article/$ADM_ART_ID/offline" $AT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /admin/article/{id}/offline"; else bad "POST offline HTTP $CODE -> $BODY"; fi

  R=$(call POST "/admin/article/$ADM_ART_ID/reject" $AT -H "Content-Type: application/json" -d '{"reason":"不合格"}'); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /admin/article/{id}/reject"; else bad "POST reject HTTP $CODE -> $BODY"; fi

  R=$(call DELETE "/admin/article/$ADM_ART_ID" $AT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "DELETE /admin/article/{id}"; else bad "DELETE article HTTP $CODE -> $BODY"; fi
fi

# ---------- 管理端: 分类 ----------
R=$(call GET "/admin/category/page?page=1&pageSize=5" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/category/page"; else bad "GET /admin/category/page HTTP $CODE -> $BODY"; fi

R=$(call POST "/admin/category" $AT -H "Content-Type: application/json" -d '{"name":"冒烟分类","sort":99}'); CODE=${R%%|*}; BODY=${R#*|}
CAT_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ "$CODE" = "200" ]; then ok "POST /admin/category (新建) -> id=$CAT_ID"; else bad "POST /admin/category HTTP $CODE -> $BODY"; fi

if [ -n "$CAT_ID" ]; then
  R=$(call PUT "/admin/category" $AT -H "Content-Type: application/json" -d "{\"id\":$CAT_ID,\"name\":\"冒烟分类改\",\"sort\":98}"); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "PUT /admin/category"; else bad "PUT /admin/category HTTP $CODE -> $BODY"; fi

  R=$(call POST "/admin/category/status/0" $AT -H "Content-Type: application/json" -d "{\"id\":$CAT_ID}"); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "POST /admin/category/status/{status}"; else bad "POST category/status HTTP $CODE -> $BODY"; fi

  R=$(call DELETE "/admin/category/$CAT_ID" $AT); CODE=${R%%|*}; BODY=${R#*|}
  if [ "$CODE" = "200" ]; then ok "DELETE /admin/category/{id}"; else bad "DELETE category HTTP $CODE -> $BODY"; fi
fi

# ---------- 管理端: dashboard / statistics / log / auth ----------
R=$(call GET "/admin/dashboard/today" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/dashboard/today"; else bad "GET dashboard/today HTTP $CODE -> $BODY"; fi

R=$(call GET "/admin/statistics/read" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/statistics/read"; else bad "GET statistics/read HTTP $CODE -> $BODY"; fi

R=$(call GET "/admin/statistics/publish" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/statistics/publish"; else bad "GET statistics/publish HTTP $CODE -> $BODY"; fi

R=$(call GET "/admin/statistics/category-distribution" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/statistics/category-distribution"; else bad "GET statistics/category-distribution HTTP $CODE -> $BODY"; fi

R=$(call GET "/admin/log/page?page=1&pageSize=5" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/log/page"; else bad "GET log/page HTTP $CODE -> $BODY"; fi

R=$(call GET "/admin/auth/info" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "GET /admin/auth/info"; else bad "GET admin/auth/info HTTP $CODE -> $BODY"; fi

R=$(call POST "/admin/auth/logout" $AT); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /admin/auth/logout"; else bad "POST admin/auth/logout HTTP $CODE -> $BODY"; fi

R=$(call POST "/admin/auth/status/0" $AT -H "Content-Type: application/json" -d '{"id":2}'); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /admin/auth/status/{status}"; else bad "POST admin/auth/status HTTP $CODE -> $BODY"; fi
# 还原 zhangsan 状态
R=$(call POST "/admin/auth/status/1" $AT -H "Content-Type: application/json" -d '{"id":2}'); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" = "200" ]; then ok "POST /admin/auth/status/1 (还原)"; else info "还原 status HTTP $CODE -> $BODY"; fi

# ---------- 鉴权拦截校验(应 401/未登录) ----------
R=$(call GET "/user/user/info"); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" != "200" ]; then ok "未带 token 访问 /user/user/info 被拦截 (HTTP $CODE)"; else bad "未鉴权竟放行 /user/user/info (HTTP $CODE)"; fi

R=$(call GET "/admin/article/page"); CODE=${R%%|*}; BODY=${R#*|}
if [ "$CODE" != "200" ]; then ok "未带 token 访问 /admin/article/page 被拦截 (HTTP $CODE)"; else bad "未鉴权竟放行 /admin/article/page"; fi

# ---------- 支付宝回调(模拟验签失败应返回 false) ----------
R=$(call POST "/api/v1/alipay/alipay_notify_url" -H "Content-Type: application/x-www-form-urlencoded" -d "trade_status=TRADE_SUCCESS&out_trade_no=TEST123&sign=bad&sign_type=RSA2"); CODE=${R%%|*}; BODY=${R#*|}
info "POST /api/v1/alipay/alipay_notify_url (伪造签名) HTTP $CODE -> '$BODY' (预期 false/校验失败)"

echo ""
echo "=================================================="
for l in "${lines[@]}"; do echo -e "$l"; done
echo "=================================================="
echo -e "${GREEN}通过: $pass${NC}   ${RED}失败: $fail${NC}"
echo "=================================================="
