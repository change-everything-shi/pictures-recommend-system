import os
import uuid
import shutil
import hashlib
import pymysql

# ===== 根据你的实际 MySQL 配置修改 =====
DB_HOST = "localhost"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "root"
DB_NAME = "test"  # 如果不是 test，就改成你的库名
# ==================================

# 你本机截图目录
SCREENSHOT_DIR = r"C:\Users\28055\Pictures\Screenshots"


def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest()


def pick_images(folder: str, limit: int = 100):
    if not os.path.isdir(folder):
        raise RuntimeError(f"截图目录不存在: {folder}")
    exts = {".png", ".jpg", ".jpeg", ".webp"}
    files = []
    for name in sorted(os.listdir(folder)):
        path = os.path.join(folder, name)
        if not os.path.isfile(path):
            continue
        ext = os.path.splitext(name)[1].lower()
        if ext in exts:
            files.append(path)
    return files[:limit]


def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(base_dir, os.pardir))
    upload_dir = os.path.join(project_root, "uploads", "photos")
    os.makedirs(upload_dir, exist_ok=True)

    images = pick_images(SCREENSHOT_DIR, 100)
    if not images:
        print(f"在 {SCREENSHOT_DIR} 下没有找到任何图片（png/jpg/jpeg/webp）")
        return

    # 所有 demo 用户统一密码：123456
    pwd_hash = sha256_hex("123456")

    # 10 个标签主题，循环分配给图片
    categories = [
        dict(
            tags="游戏,数码",
            title="游戏时刻",
            desc="玩游戏的截图，屏幕上是游戏画面或相关界面。",
        ),
        dict(
            tags="数码,电脑",
            title="桌面装备",
            desc="和电脑、桌面、软件界面相关的截图。",
        ),
        dict(
            tags="家具,家居",
            title="家居一角",
            desc="与家居、装修、房间布局相关的图片。",
        ),
        dict(
            tags="美女,自拍",
            title="写真瞬间",
            desc="人物照片或自拍相关的图片。",
        ),
        dict(
            tags="帅哥,自拍",
            title="日常自拍",
            desc="人物或自拍、日常合影相关的图片。",
        ),
        dict(
            tags="旅行,风景",
            title="旅行风景",
            desc="风景、地图、出行路线等旅行相关内容。",
        ),
        dict(
            tags="美食,日常",
            title="餐食记忆",
            desc="食物、菜单、点餐页面相关的图片。",
        ),
        dict(
            tags="运动,健身",
            title="运动瞬间",
            desc="运动、锻炼、健康 APP 截图等相关内容。",
        ),
        dict(
            tags="宠物,萌宠",
            title="宠物时光",
            desc="宠物、萌物相关的图片。",
        ),
        dict(
            tags="风景,天空",
            title="随手一拍",
            desc="随手拍的风景、桌面壁纸、天空等图片。",
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

    total = min(100, len(images))
    print(f"共找到 {len(images)} 张图片，本次使用前 {total} 张。")

    for i in range(1, total + 1):
        src = images[i - 1]
        cat = categories[(i - 1) % len(categories)]
        uname = f"local_user_{i:03d}"

        # 确保用户存在
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

        # 拷贝图片到 uploads/photos
        ext = os.path.splitext(src)[1].lower() or ".jpg"
        filename = f"{uuid.uuid4().hex}{ext}"
        dst = os.path.join(upload_dir, filename)
        shutil.copy2(src, dst)
        image_url = f"/uploads/photos/{filename}"

        title = f"{cat['title']} #{i}"

        cursor.execute(
            "INSERT INTO photo(user_id, title, description, tags, image_url, create_time, update_time) "
            "VALUES (%s,%s,%s,%s,%s, NOW(), NOW())",
            (user_id, title, cat['desc'], cat['tags'], image_url),
        )
        conn.commit()

        print(f"[{i}/{total}] 用户 {uname} -> {image_url} ({cat['tags']})")

    cursor.close()
    conn.close()
    print("完成：已创建用户 local_user_001 ~ local_user_%03d，并各自发布 1 张本地图片。" % total)


if __name__ == "__main__":
    main()

