def quicksort(arr, low, high):
    """
    快速排序主函数
    """
    if low < high:
        # 进行分区，返回基准的最终位置
        pi = partition(arr, low, high)
        
        # 递归排序基准左边的子数组
        quicksort(arr, low, pi - 1)
        # 递归排序基准右边的子数组
        quicksort(arr, pi + 1, high)

def partition(arr, low, high):
    """
    分区函数 (Lomuto 分区方案，以最后一个元素为基准)
    """
    pivot = arr[high]  # 选择最后一个元素作为基准
    i = low - 1        # 小于基准的区域的右边界

    for j in range(low, high):
        # 如果当前元素小于或等于基准
        if arr[j] <= pivot:
            i += 1
            arr[i], arr[j] = arr[j], arr[i]  # 交换

    # 将基准放到正确位置
    arr[i + 1], arr[high] = arr[high], arr[i + 1]
    return i + 1

# 使用示例
arr = [3, 6, 8, 10, 1, 2, 1]
print(f"原数组: {arr}")
quicksort(arr, 0, len(arr) - 1)
print(f"排序后: {arr}")