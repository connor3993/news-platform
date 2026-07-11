#!/usr/bin/env python3
# newsPlatform 全接口冒烟测试 (按真实 Result{code,msg,data} 结构断言)
import json, subprocess, sys, time

BASE = "http://127.0.0.1:8080"
PASS, FAIL = 0, 0
report = []

def pick_id(d):
    """从 data 中稳妥取 id（data 可能是 int 或 dict）"""
    data = d.get("data") if isinstance(d, dict) else None
    if isinstance(data, int):
        return data
    if isinstance(data, dict):
        return data.get("id")
    return None

def curl(method, path, token=None, body=None, headers=None):
    cmd = ["curl", "-s", "-w", "\n%{http_code}", "-X", method, BASE + path]
    hs = dict(headers or {})
    if token:
        hs["token"] = token
    for k, v in hs.items():
        cmd += ["-H", f"{k}: {v}"]
    if body is not None:
        cmd += ["-H", "Content-Type: application/json", "-d", json.dumps(body, ensure_ascii=False)]
    out = subprocess.run(cmd, capture_output=True, text=True).stdout
    parts = out.rsplit("\n", 1)
    body_str = parts[0] if len(parts) == 2 else ""
    http = parts[1] if len(parts) == 2 else "000"
    try:
        data = json.loads(body_str) if body_str.strip() else {}
    except Exception:
        data = {"_raw": body_str}
    return int(http), data

def ok(msg):
    global PASS
    PASS += 1; report.append(f"✅ {msg}")

def bad(msg):
    global FAIL
    FAIL += 1; report.append(f"❌ {msg}")

def info(msg):
    report.append(f"ℹ️  {msg}")

def biz_ok(d):
    return isinstance(d, dict) and d.get("code") == 1

# 0. 健康检查
h, d = curl("GET", "/api/v1/weixin/portal/receive?signature=x&timestamp=y&nonce=z&echostr=")
if h != 0:
    ok(f"服务可达 (HTTP {h})")
else:
    bad("服务不可达")

# 文档接口1: 微信二维码 ticket
h, d = curl("GET", "/api/v1/login/weixin_qrcode_ticket")
if biz_ok(d) and d.get("data"):
    ok(f"文档接口1 GET /api/v1/login/weixin_qrcode_ticket -> 获得 ticket: {str(d['data'])[:20]}...")
else:
    bad(f"微信二维码ticket HTTP {h} -> {d}")

# 文档接口2: 检查登录(未扫码, 应 data=null 但 code=1)
h, d = curl("GET", "/api/v1/login/check_login?ticket=not_exist_123")
if h == 200 and biz_ok(d):
    ok(f"文档接口2 GET /api/v1/login/check_login (未扫码) -> data={d.get('data')}")
else:
    bad(f"检查登录 HTTP {h} -> {d}")

# 文档接口3: 公众号验签(GET, 错误签名应返回空/校验失败)
h, d = curl("GET", "/api/v1/weixin/portal/receive?signature=bad&timestamp=1&nonce=2&echostr=echo")
info(f"文档接口3 GET /api/v1/weixin/portal/receive (验签) HTTP {h} body={d if '_raw' in d else d.get('data')}")

# 登录拿 token
h, d = curl("POST", "/user/auth/login", body={"username": "zhangsan", "password": "123456"})
if biz_ok(d) and d["data"].get("token"):
    USER_TOKEN = d["data"]["token"]; ok("用户登录 zhangsan/123456 -> 获得 token")
else:
    USER_TOKEN = None; bad(f"用户登录失败 HTTP {h} -> {d}")

h, d = curl("POST", "/admin/auth/login", body={"username": "admin", "password": "123456"})
if biz_ok(d) and d["data"].get("token"):
    ADMIN_TOKEN = d["data"]["token"]; ADMIN_ID = d["data"].get("id") or 2; ok("管理员登录 admin/123456 -> 获得 token")
else:
    ADMIN_TOKEN = None; ADMIN_ID = 2; bad(f"管理员登录失败 HTTP {h} -> {d}")

# 用户注册
h, d = curl("POST", "/user/auth/register", body={"username": "test_smoke_001", "password": "123456", "nickname": "冒烟"})
ok(f"POST /user/auth/register -> code={d.get('code')}") if h == 200 else bad(f"register HTTP {h} -> {d}")

# 用户资料
h, d = curl("GET", "/user/user/info", token=USER_TOKEN)
ok("GET /user/user/info") if biz_ok(d) else bad(f"GET /user/user/info HTTP {h} -> {d}")
h, d = curl("PUT", "/user/user/info", token=USER_TOKEN, body={"nickname": "张三改", "avatar": "https://x.com/a.png"})
ok("PUT /user/user/info") if biz_ok(d) else bad(f"PUT /user/user/info HTTP {h} -> {d}")

# 用户文章
for p in ["/user/article/page?page=1&pageSize=5", "/user/article/5", "/user/article/mine/page?page=1&pageSize=5", "/user/article/favorite/page?page=1&pageSize=5"]:
    h, d = curl("GET", p, token=USER_TOKEN)
    ok(f"GET {p.split('?')[0]}") if biz_ok(d) else bad(f"GET {p} HTTP {h} -> {d}")
h, d = curl("POST", "/user/article", token=USER_TOKEN, body={"title": "冒烟草稿", "summary": "t", "categoryId": 1, "content": "hello", "isPaid": 0})
ok("POST /user/article (建草稿)") if biz_ok(d) else bad(f"POST /user/article HTTP {h} -> {d}")
for act in ["like", "dislike", "favorite", "read"]:
    h, d = curl("POST", f"/user/article/1/{act}", token=USER_TOKEN)
    ok(f"POST /user/article/1/{act}") if biz_ok(d) else bad(f"POST /user/article/1/{act} HTTP {h} -> {d}")

# 用户分类
h, d = curl("GET", "/user/category/list")
ok("GET /user/category/list") if biz_ok(d) else bad(f"GET /user/category/list HTTP {h} -> {d}")

# 支付(文档接口4 的等价: 下单) - 走 mock, 不真连支付宝
h, d = curl("POST", "/user/pay/create", token=USER_TOKEN, body={"articleId": 5})
ORDER_ID = (d.get("data", {}) or {}).get("orderId") if biz_ok(d) else None
ok(f"POST /user/pay/create (articleId=5) -> orderId={ORDER_ID}") if biz_ok(d) else bad(f"POST /user/pay/create HTTP {h} -> {d}")
if ORDER_ID:
    h, d = curl("GET", f"/user/pay/order/{ORDER_ID}", token=USER_TOKEN)
    ok("GET /user/pay/order/{id}") if biz_ok(d) else bad(f"GET order HTTP {h} -> {d}")
    h, d = curl("POST", f"/user/pay/mock-success?orderId={ORDER_ID}", token=USER_TOKEN)
    ok("POST /user/pay/mock-success (mock支付成功)") if biz_ok(d) else bad(f"POST mock-success HTTP {h} -> {d}")
    h, d = curl("GET", "/user/pay/purchased?articleId=5", token=USER_TOKEN)
    ok("GET /user/pay/purchased?articleId=5") if biz_ok(d) else bad(f"GET purchased HTTP {h} -> {d}")

# 管理端文章
h, d = curl("GET", "/admin/article/page?page=1&pageSize=5", token=ADMIN_TOKEN)
ok("GET /admin/article/page") if biz_ok(d) else bad(f"GET /admin/article/page HTTP {h} -> {d}")
h, d = curl("GET", "/admin/article/5", token=ADMIN_TOKEN)
ok("GET /admin/article/{id}") if biz_ok(d) else bad(f"GET /admin/article/5 HTTP {h} -> {d}")
h, d = curl("POST", "/admin/article", token=ADMIN_TOKEN, body={"title": "管理端冒烟", "summary": "t", "categoryId": 1, "content": "c", "isPaid": 0})
AID = pick_id(d) if biz_ok(d) else None
ok(f"POST /admin/article (新建) -> id={AID}") if biz_ok(d) else bad(f"POST /admin/article HTTP {h} -> {d}")
if AID:
    for act, meth, b in [("submit","POST",None),("approve","POST",None),("publish","POST",None),("offline","POST",None)]:
        h, d = curl(meth, f"/admin/article/{AID}/{act}", token=ADMIN_TOKEN, body=b)
        ok(f"POST /admin/article/{AID}/{act}") if biz_ok(d) else bad(f"POST {act} HTTP {h} -> {d}")
    h, d = curl("POST", f"/admin/article/{AID}/reject", token=ADMIN_TOKEN, body={"auditComment": "不合格"})
    ok(f"POST /admin/article/{AID}/reject") if biz_ok(d) else bad(f"POST reject HTTP {h} -> {d}")
    h, d = curl("PUT", "/admin/article", token=ADMIN_TOKEN, body={"id": AID, "title": "改", "categoryId": 1, "content": "c2"})
    ok("PUT /admin/article (更新)") if biz_ok(d) else bad(f"PUT article HTTP {h} -> {d}")
    h, d = curl("DELETE", f"/admin/article/{AID}", token=ADMIN_TOKEN)
    ok("DELETE /admin/article/{id}") if biz_ok(d) else bad(f"DELETE article HTTP {h} -> {d}")

# 管理端分类
CAT_NAME = f"冒烟分类_{int(time.time())}"
h, d = curl("GET", "/admin/category/page?page=1&pageSize=5", token=ADMIN_TOKEN)
ok("GET /admin/category/page") if biz_ok(d) else bad(f"GET /admin/category/page HTTP {h} -> {d}")
h, d = curl("POST", "/admin/category", token=ADMIN_TOKEN, body={"name": CAT_NAME, "sort": 99})
ok("POST /admin/category (新建)") if biz_ok(d) else bad(f"POST /admin/category HTTP {h} -> {d}")
# create 返回 Result<Void>, 按名称回查拿到新建分类 id
CID = None
if biz_ok(d):
    h2, d2 = curl("GET", "/admin/category/page?page=1&pageSize=100", token=ADMIN_TOKEN)
    recs = (d2.get("data") or {}).get("records") or [] if biz_ok(d2) else []
    for r in recs:
        if r.get("name") == CAT_NAME:
            CID = r.get("id"); break
ok(f"POST /admin/category 回查到 id={CID}") if CID else bad("未能回查新建分类 id")
if CID:
    h, d = curl("PUT", "/admin/category", token=ADMIN_TOKEN, body={"id": CID, "name": CAT_NAME + "改", "sort": 98})
    ok("PUT /admin/category") if biz_ok(d) else bad(f"PUT category HTTP {h} -> {d}")
    h, d = curl("POST", f"/admin/category/status/0?id={CID}", token=ADMIN_TOKEN)
    ok("POST /admin/category/status/{status}") if biz_ok(d) else bad(f"POST category/status HTTP {h} -> {d}")
    h, d = curl("DELETE", f"/admin/category/{CID}", token=ADMIN_TOKEN)
    ok("DELETE /admin/category/{id}") if biz_ok(d) else bad(f"DELETE category HTTP {h} -> {d}")

# 管理端 dashboard / statistics / log / auth
for p in ["/admin/dashboard/today", "/admin/statistics/read", "/admin/statistics/publish", "/admin/statistics/category-distribution", "/admin/log/page", "/admin/auth/info"]:
    h, d = curl("GET", p, token=ADMIN_TOKEN)
    ok(f"GET {p}") if biz_ok(d) else bad(f"GET {p} HTTP {h} -> {d}")
h, d = curl("POST", "/admin/auth/logout", token=ADMIN_TOKEN)
ok("POST /admin/auth/logout") if biz_ok(d) else bad(f"POST logout HTTP {h} -> {d}")
h, d = curl("POST", f"/admin/auth/status/0?id={ADMIN_ID}", token=ADMIN_TOKEN)
ok("POST /admin/auth/status/{status}") if biz_ok(d) else bad(f"POST status HTTP {h} -> {d}")
h, d = curl("POST", f"/admin/auth/status/1?id={ADMIN_ID}", token=ADMIN_TOKEN)
ok("POST /admin/auth/status/1 (还原)") if biz_ok(d) else bad(f"POST status/1 HTTP {h} -> {d}")

# 鉴权拦截: 未带 token 应返回业务码 401 (HTTP 200 + code=401)
for p in ["/user/user/info", "/admin/article/page"]:
    h, d = curl("GET", p)
    if isinstance(d, dict) and d.get("code") == 401:
        ok(f"未带token访问 {p} 被拦截 (code=401)")
    else:
        bad(f"未带token访问 {p} 未被正确拦截 HTTP {h} -> {d}")

# 支付宝回调(伪造签名, 应验签失败)
h, d = curl("POST", "/api/v1/alipay/alipay_notify_url", headers={"Content-Type": "application/x-www-form-urlencoded"}, body=None)
# 用 -d 表单
import subprocess as sp
raw = sp.run(["curl","-s","-w","\n%{http_code}","-X","POST",BASE+"/api/v1/alipay/alipay_notify_url",
              "-H","Content-Type: application/x-www-form-urlencoded",
              "-d","trade_status=TRADE_SUCCESS&out_trade_no=TEST123&sign=bad&sign_type=RSA2"],
             capture_output=True, text=True).stdout
info(f"POST /api/v1/alipay/alipay_notify_url (伪造签名) -> {raw.rsplit(chr(10),1)[0] if chr(10) in raw else raw} (预期 false/校验失败)")

print("=" * 60)
for r in report: print(r)
print("=" * 60)
print(f"通过: {PASS}   失败: {FAIL}")
sys.exit(1 if FAIL else 0)
