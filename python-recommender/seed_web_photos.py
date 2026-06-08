import os
import uuid
import hashlib
import requests
import pymysql

# ===== 这里按你的实际 MySQL 配置修改 =====
DB_HOST = "localhost"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "root"
DB_NAME = "test"   # 如果不是 test，就改成你的库名
# ======================================


def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest()


def main():
    # 确保 uploads/photos 目录存在（从项目根目录运行脚本）
    base_dir = os.path.dirname(os.path.abspath(__file__))
    upload_dir = os.path.join(base_dir, "uploads", "photos")
    os.makedirs(upload_dir, exist_ok=True)

    # 所有 demo 用户统一密码：123456
    pwd_hash = sha256_hex("123456")

    # 10 个不同主题，每个主题有 标签/标题前缀/描述/搜索关键词
    categories = [
        dict(
            tags="游戏,数码",
            title="游戏时刻",
            desc="今晚开黑打游戏，屏幕和键盘灯光很炫。",
            query="gaming,esports,pc"
        ),
        dict(
            tags="数码,电脑",
            title="桌面装备",
            desc="新买的显示器和机械键盘，桌面布置完成。",
            query="desk,computer,setup"
        ),
        dict(
            tags="家具,家居",
            title="客厅一角",
            desc="客厅沙发和茶几，准备周末躺平追剧。",
            query="living room,furniture,sofa"
        ),
        dict(
            tags="美女,自拍",
            title="街拍写真",
            desc="在街头拍的一组人像写真，光线很好。",
            query="girl,portrait,street"
        ),
        dict(
            tags="帅哥,自拍",
            title="健身自拍",
            desc="健身房里练完举铁，随手拍一张。",
            query="man,portrait,gym"
        ),
        dict(
            tags="旅行,风景",
            title="旅行风景",
            desc="出去旅行路上的风景，天空和山都很好看。",
            query="travel,landscape,mountain"
        ),
        dict(
            tags="美食,日常",
            title="今日美食",
            desc="今天中午吃的美食，看着就很有食欲。",
            query="food,meal,restaurant"
        ),
        dict(
            tags="运动,篮球",
            title="球场瞬间",
            desc="篮球场上挥汗如雨，努力投篮。",
            query="basketball,sports"
        ),
        dict(
            tags="宠物,萌宠",
            title="我的宠物",
            desc="家里的小猫小狗趴在沙发上晒太阳。",
            query="pet,cat,dog"
        ),
        dict(
            tags="风景,天空",
            title="随手一拍",
            desc="黄昏时分的天空和城市剪影，颜色很柔和。",
            query="sky,sunset,city"
        ),
    ]

    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.Cursor,
    )
    cursor = conn.cursor()

    for i in range(1, 101):
        uname = f"web_user_{i:03d}"
        cat = categories[(i - 1) % len(categories)]

        # 1. 确保用户存在
        cursor.execute("SELECT id FROM user_account WHERE username=%s", (uname,))
        row = cursor.fetchone()
        if row:
            user_id = row[0]
        else:
            cursor.execute(
                "INSERT INTO user_account(username, password_hash, create_time, update_time) "
                "VALUES (%s, %s, NOW(), NOW())",
                (uname, pwd_hash),
            )
            user_id = cursor.lastrowid
            conn.commit()

        # 2. 从 Unsplash 拉一张对应主题的图片（需要能访问外网）
        url = f"https://source.unsplash.com/featured/800x600/?{cat['query']}"
        print(f"[{i}/100] 下载图片: {url}")
        try:
            resp = requests.get(url, timeout=20)
            resp.raise_for_status()
        except Exception as e:
            print(f"  下载失败，跳过该用户: {e}")
            continue

        # 3. 保存到 uploads/photos 目录
        ext = ".jpg"
        filename = f"{uuid.uuid4().hex}{ext}"
        file_path = os.path.join(upload_dir, filename)
        with open(file_path, "wb") as f:
            f.write(resp.content)

        image_url = f"/uploads/photos/{filename}"

        # 4. 插入 photo 记录（每个用户 1 张）
        title = f"{cat['title']} #{i}"
        cursor.execute(
            "INSERT INTO photo(user_id, title, description, tags, image_url, create_time, update_time) "
            "VALUES (%s,%s,%s,%s,%s, NOW(), NOW())",
            (user_id, title, cat["desc"], cat["tags"], image_url),
        )
        conn.commit()

    cursor.close()
    conn.close()
    print("全部完成，可以去页面上看推荐效果了。")


if __name__ == "__main__":
    main()