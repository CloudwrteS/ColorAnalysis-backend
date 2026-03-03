# 简单的 Flask HTTP 服务，将现有的算法封装为 REST 接口
from flask import Flask, request, send_file
from algo.canny import detect_regions  # 假设该函数接受 PIL.Image 并返回 PIL.Image
import io
from PIL import Image

app = Flask(__name__)

@app.route('/canny', methods=['POST'])
def canny():
    if 'image' not in request.files:
        return 'missing image file', 400
    file = request.files['image']
    img = Image.open(file.stream).convert('RGB')
    result = detect_regions(img)

    buf = io.BytesIO()
    result.save(buf, format='PNG')
    buf.seek(0)
    return send_file(buf, mimetype='image/png')

if __name__ == '__main__':
    # 运行之前请安装 flask: pip install flask
    app.run(host='0.0.0.0', port=5000)
