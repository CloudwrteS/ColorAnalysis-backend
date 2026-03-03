# 简单的 Flask HTTP 服务，将现有的算法封装为 REST 接口
from flask import Flask, request, send_file, jsonify
from algo import canny, image_correction, all_hsv
import numpy as np
import cv2
import io
from PIL import Image
import tempfile
import os

app = Flask(__name__)

# ---------- Canny polygon detection (returns JSON) ----------
@app.route('/canny', methods=['POST'])
def canny_api():
    if 'image' not in request.files:
        return 'missing image file', 400
    file = request.files['image']
    config = request.form.get('config')
    try:
        cfg = eval(config) if config else {}
    except Exception:
        cfg = {}
    img = Image.open(file.stream).convert('RGB')
    img_np = np.array(img)[:, :, ::-1]  # RGB -> BGR for cv2
    regions, metadata = canny.detect_regions(img_np, cfg)
    return jsonify({'regions': regions, 'metadata': metadata})

# ---------- Image correction endpoints ----------
@app.route('/image/correction/points', methods=['POST'])
def detect_points_api():
    if 'image' not in request.files:
        return 'missing image file', 400
    tmp = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
    request.files['image'].save(tmp.name)
    points = image_correction.detect_points(tmp.name)
    os.unlink(tmp.name)
    if points is None:
        return jsonify({'error': 'cannot detect points'}), 400
    return jsonify({'points': points.tolist()})

@app.route('/image/correction/align', methods=['POST'])
def align_image_api():
    # expects two files: model and image
    if 'model' not in request.files or 'image' not in request.files:
        return 'need both model and image files', 400
    model_tmp = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
    image_tmp = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
    model_file = request.files['model']; image_file = request.files['image']
    model_file.save(model_tmp.name)
    image_file.save(image_tmp.name)
    out_dir = tempfile.mkdtemp()
    image_correction.align_image(model_tmp.name, image_tmp.name, out_dir)
    # assume output file has same basename as original image
    aligned_path = os.path.join(out_dir, os.path.basename(image_tmp.name))
    if not os.path.exists(aligned_path):
        return jsonify({'error': 'alignment failed'}), 500
    return send_file(aligned_path, mimetype='image/png')

# ---------- HSV processing ----------
@app.route('/hsv/process_image', methods=['POST'])
def hsv_process_api():
    if 'image' not in request.files or 'mask' not in request.files:
        return 'need image and mask files', 400
    img_tmp = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
    mask_tmp = tempfile.NamedTemporaryFile(delete=False, suffix='.png')
    request.files['image'].save(img_tmp.name)
    request.files['mask'].save(mask_tmp.name)
    # call process_image which returns (final_masked_image, refined_mask)
    img = cv2.imread(img_tmp.name)
    result_img, _ = all_hsv.process_image(img, mask_tmp.name)
    buf = cv2.imencode('.png', result_img)[1].tobytes()
    return send_file(io.BytesIO(buf), mimetype='image/png')

if __name__ == '__main__':
    # 运行之前请安装 flask: pip install flask
    app.run(host='0.0.0.0', port=5000)

if __name__ == '__main__':
    # 运行之前请安装 flask: pip install flask
    app.run(host='0.0.0.0', port=5000)
