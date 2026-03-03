import pandas as pd
import numpy as np
import json
from scipy.stats import entropy

# 示例标签集（根据需求更改）
region_id_list = [
  "01-00-00-00", "00-01-00-00", "00-02-00-00", "00-03-00-00", "00-04-00-00",
  "00-00-01-00", "00-00-01-01", "00-00-01-02", "00-00-02-00", "00-00-02-01",
  "00-00-02-02", "00-00-03-00", "00-00-04-00", "00-00-04-01", "00-00-04-02",
  "00-00-04-03", "00-00-04-04", "00-00-04-05", "00-00-04-06", "00-00-04-07",
  "00-00-04-08", "00-00-05-00", "00-00-05-01", "00-00-05-02", "00-00-06-00",
  "00-00-06-01", "00-00-06-02", "00-00-07-00", "00-00-08-00", "00-00-08-01",
  "00-00-08-02", "00-00-08-03", "00-00-08-04", "00-00-08-05", "00-00-08-06",
  "00-00-08-07", "00-00-08-08", "00-00-09-00", "00-00-09-01", "00-00-09-02",
  "00-00-10-00", "00-00-05-03", "00-00-05-04", "00-00-05-05", "00-00-05-06",
  "00-00-06-03", "00-00-06-04", "00-00-06-05", "00-00-06-06"
]


# 逐块读取文件并处理
chunk_size = 50  # 每块读取的行数
aggregated_results = []  # 用于存储最终结果

# 按块读取文件并处理
for chunk in pd.read_csv('lj2_all_hsv_results.csv', chunksize=chunk_size):
    # 将HSV像素字符串转换为列表
    chunk['hsv_pixels'] = chunk['hsv_pixels'].apply(json.loads)

    # 根据 region_id 提取对应区域的 HSV 值
    def extract_region_hsv(hsv_pixels, region_id, region_id_list):
        if region_id in region_id_list:
            return np.array(hsv_pixels)
        return None

    # 计算H、S、V通道的熵值
    def calculate_entropy(hsv_array):
        if hsv_array is not None and len(hsv_array) > 0:
            H = hsv_array[:, 0]  # Hue
            S = hsv_array[:, 1]  # Saturation
            V = hsv_array[:, 2]  # Value

            # 计算每个通道的熵
            h_entropy = entropy(np.histogram(H, bins=36)[0])
            s_entropy = entropy(np.histogram(S, bins=10)[0])
            v_entropy = entropy(np.histogram(V, bins=10)[0])

            return h_entropy, s_entropy, v_entropy
        return None, None, None

    # 处理每一行数据
    for idx, row in chunk.iterrows():
        # 提取当前图像的HSV像素和region_id
        hsv_pixels = row['hsv_pixels']
        region_id = row['region_id']

        # 获取每个区域的HSV数据
        hsv_data = extract_region_hsv(hsv_pixels, region_id, region_id_list)

        # 如果该区域在标签集里
        if hsv_data is not None:
            # 计算该区域的H、S、V通道的熵值
            h_entropy, s_entropy, v_entropy = calculate_entropy(hsv_data)

            # 如果计算成功，将熵值添加到结果中
            if h_entropy is not None and s_entropy is not None and v_entropy is not None:
                aggregated_results.append([row['image_name'], row['region_id'], row['region_alias'], h_entropy, s_entropy, v_entropy])

    # 处理完当前块后清空数据，释放内存
    del chunk

# 最终输出每个区域的熵值
aggregated_df = pd.DataFrame(aggregated_results, columns=['image_name', 'region_id', 'region_alias', 'H_entropy', 'S_entropy', 'V_entropy'])

# 存储为CSV文件
aggregated_df.to_csv('lj2_image_entropy_region_results.csv', index=False)

# 输出结果
print(aggregated_df)
