# 实验设计

从POIs中随机选取点对作为路径查询的起点与终点

## 实验步骤

### 1. 构建缓存

1. 点集中随机选择起点终点生成历史查询路径与相应最短路径（需使用Dijkstra或A*计算最短路径）
2. 直接使用历史查询的最短路径（最短路径的起点、终点与查询的一致）
3. 依赖Sharing ability per edge构建缓存
4. 构建三大序列EII、ENI、EPI并使用R-tree组织序列

注：使用node数据生成的最短路径不符合实际的正态分布，参考价值有限



更合理的设计：

1. 根据POIs生成历史查询
2. 映射到图中node，计算最短路径，得出历史查询的对应最短路径
3. 根据以上信息，计算每条最短路径的sharing ability per edge，构建缓存与序列
4. 使用POIs生成新查询需求，使用缓存应答

### 2. 使用缓存

根据点集生成路径查询条目，使用缓存进行应答

1. 根据点在R-tree中定位候选边（EdgeLocating算法）
2. 在缓存中查询候选边
   1. EII中定位起点、终点映射的边，找不到cache miss
   2. EPI中检索路径，交集为空则cache miss，不为空则逐步构建出完整路径

### 3. 统计数据

统计响应时间、命中率

## 实验数据

因数据量大，计算费时，为便于测试，生成一次缓存与索引后可以使用文件保存

### 现有路径数据集

1. 加利福尼亚路网边（边ID、起点ID、终点ID、欧氏距离）
2. 加利福尼亚路网点（点ID、经度、维度）
3. 加利福尼亚POI点（经度、维度、类型ID）
4. 加利福尼亚POI点原类型名（类型名、经度、维度）
5. 合并POI点路网			

### 实验一：路径查询的数量影响

| 变量类型                 | 数量(k)   |
| ------------------------ | --------- |
| 用于构建缓存的历史路径量 | 10        |
| 缓存节点容量             | 500       |
| 路径查询量               | [1,5]/0.5 |

注：[a,b]/c  取点区间为a~b，每c取一点

### 实验二：缓存大小的影响

| 变量类型                 | 数量(k)      |
| ------------------------ | ------------ |
| 用于构建缓存的历史路径量 | 10           |
| 缓存节点容量             | [150,500]/50 |
| 路径查询量               | 3            |

### 实验三：历史查询量的影响

| 变量类型                 | 数量(k)  |
| ------------------------ | -------- |
| 用于构建缓存的历史路径量 | [5,15]/1 |
| 缓存节点容量             | 500      |
| 路径查询量               | 3        |
