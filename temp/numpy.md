## ndarray (n-dimensional array)

다양한 차원의 `array` 를 `numpy` 로 쉽게 정의 가능  

제공되는 다양한 함수를 통해 쉽게 데이터 연산 가능  

```py
import numpy as np   #numpy는 기니까 np로 줄여서 사용
a = np.array([0,1,2,3,4,5,6,7,8,9])
print(type(a)) # <class 'numpy.ndarray'>
print(a.ndim) # 1
print(a.shape) # (10,)

b = np.array([[0, 1, 2, 3], [4, 5, 6, 7]])
b[0,0]  # first row , first col
b[0,1]  # first row , second col
b[-1,-1] # last row , last col
b[0,:]  # first row(전체)
b[:,1]  # second col (전체)
b[1,1:] # second row의 second col 부터 끝까지
b[:,0:3] #모든 row의 0:3까지 col
```


## 최소값 최대값 index 

`np.argmin(), np.argmax()`  

```py
print(net.W)
print(np.argmax(net.W))
```

```
[[-2.30632234 -0.40351578 -1.6686059 ]
 [ 0.86243116 -0.15211806 -1.40741961]]
3
```

## np.nditer - 순회  

`np.nditer`

```py
x = np.array([[1, 2, 3], [4, 5, 6]])
it = np.nditer(x, flags=["multi_index"], op_flags=['readwrite'])
while not it.finished:
    print(x[it.multi_index])
    it.iternext()
# 1
# 2
# 3
# 4
# 5
# 6
```

다차원 배열에 순차적으로 접근

## np.sum

```py
x = np.array([[1, 2, 3],[1, 2, 3]])
print(np.sum(x)) # 12
```

모든 요소의 합을 출력한다.  

```py
y = np.array([1, 2, 3])
t = np.array([1, 2, 3])
print(y == t) # [ True  True  True]
print(np.sum(y == t)) # 3
```

위와 같이 True 를 1로 계산하기에 동일위치 동일값이 몇개인지 알 수 있다.  

## axis

> axis: (중심) 축

넘파이의 매우 많음 함수에서 제공되는 변수로 말그대로 축을 사용해서 연산할 때 사용한다.  

> http://taewan.kim/post/numpy_sum_axis/

위 블로그에 매우 상세히 설명되어있음  

```py
import numpy as np


arr = np.arange(0, 32)
arr = arr.reshape([4,2,4])
print(arr)
# [[[ 0  1  2  3]
#   [ 4  5  6  7]]

#  [[ 8  9 10 11]
#   [12 13 14 15]]

#  [[16 17 18 19]
#   [20 21 22 23]]

#  [[24 25 26 27]
#   [28 29 30 31]]]
```

위와같이 3차원 matrix 를 그림으로 표현하면 아래와 같다.   

![np1](image/np1.jpg)  

좌측아래부터 [0,0,0] 으로 그려놓았다.   

```py
print(arr.sum(axis=0))
# [[48 52 56 60]
#  [64 68 72 76]]

print(arr.sum(axis=1))
# [[ 4  6  8 10]
#  [20 22 24 26]
#  [36 38 40 42]
#  [52 54 56 58]]

print(arr.sum(axis=2))
# [[  6  22]
#  [ 38  54]
#  [ 70  86]
#  [102 118]]
```

각 축으로 덧셈을한 결과를 아래 그림처럼 나타낼 수 있다.  

![np2](image/np2.jpg)  

![np3](image/np3.jpg)  

![np4](image/np4.jpg)  


## np.expand_dims

expend dimension (차원 추가)

```py
x = np.array([1, 2])
x.shape
# (2,)
```


## np.random

### randn - 표준정규분포

```python
import numpy as np

a = np.random.randn(5) # 표준 정규분포 난수 5개
b = np.random.randn(2, 3) # 2차원 배열형태의 난수 6개

print(a)
print(b)
```

```
[0.28140916 0.47827139 0.96844448 1.48415209 0.78947828]
[[-0.35707587 -0.6925682   1.0248466 ]
 [-1.77603803 -0.66903502  1.23282111]]
```

### rand

0부터 1사이의 균일 분포에서 난수 matrix array생성

```py
np.random.rand(6)
```
```
array([0.21696513, 0.99735417, 0.83609081, 0.99821954, 0.07308539, 0.29846808])
```

### randint

`low`, `high` 사이 균일분포에서 size 만큼 실수형 난수 생성, size 미지정시 단순 실수하나 반환, 지정시 배열형태 반환  

```py
np.random.randint(low, high, size)
```




### uniform

`low`, `high` 사이 균일분포에서 size 만큼 실수형 난수 생성, size 미지정시 단순 실수하나 반환, 지정시 배열형태 반환  


```py
np.random.uniform(low, high, size)
```

### choice

`np.random.choice()` 함수로 임의(무작위, 확률) 추출 (random sampling) 가능  

![np4](image/np5.png)  

## flatten

다차원 배열을 1차원 배열로 평평하게 펴주는 ravel(), flatten() 함수

```py
b = np.random.randn(2, 3) # 2차원 배열형태의 난수 6개
print(b.flatten())
print(b.ravel())
```

```
[ 0.45883847  0.2985751   1.68589697 -0.34460654 -0.84546482 -0.69634623]
[ 0.45883847  0.2985751   1.68589697 -0.34460654 -0.84546482 -0.69634623]
```

`ravel` 과 `flatten` 의 차이는 값의 복사 여부  

`flatten` 의 경우 원소의 완전한 복사가 이루어지지만
`ravel` 의 경우 원소의 참조가 끊기지 않아 원본 변경시 같이 변경된다.  

### 정렬

```py
x = np.arange(12).reshape(3, 4)
print(x)
print(x.flatten(order='C')) # default, row 우선
print(x.flatten(order='F')) # column 과 같은 순서
print(x.flatten(order='K')) # 메모리 발생 순서
```

```
[[ 0  1  2  3]
 [ 4  5  6  7]
 [ 8  9 10 11]]
[ 0  1  2  3  4  5  6  7  8  9 10 11]
[ 0  4  8  1  5  9  2  6 10  3  7 11]
[ 0  1  2  3  4  5  6  7  8  9 10 11]
```

## np.hstack, np.vstack

배열 이어붙이기

```py
import numpy as np

# 배열 준비
a = np.array([1,2,3])
b = np.array([4,5,6])

c = np.hstack((a,b))
d = np.vstack((a,b))
print(c)
print(d)
"""
[1 2 3 4 5 6]
[[1 2 3]
 [4 5 6]]
"""
```


## np.concatenate

![np4](image/np6.png)  

```py
np.concatenate((hs, out), axis=2)
```


## repeat

열과 행을 복사하는 함수

```py
numpy.repeat(a, repeats, axis=None)
```

`axis` 는 합치는 방향

```py
arr = np.repeat(5,3)
print(arr)
# array([5, 5, 5])
```


## broadcaasting

![np4](image/np7.png)  
