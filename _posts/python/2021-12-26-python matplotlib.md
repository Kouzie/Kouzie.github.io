---
title:  "python matplotlib!"

read_time: false
share: false
author_profile: false
# # classes: wide

categories:
  - python

toc: true
toc_sticky: true

---

## 설치

```py
import matplotlib.pyplot as plt
```

## hist - 히스토그램

```py
for i, a in activations.items():
    # subplot(nrows, ncols, index)
    plt.subplot(1, len(activations), i+1) # 1행 5열 i번째 
    plt.title(str(i+1) + "-layer")
    if i != 0: plt.yticks([], []) # 첫번째 그래프외에는 y눈금 삭제
    plt.xlim(0, 1)
    plt.ylim(0, 7000)
    plt.hist(a.flatten(), 30, range=(0, 1))
plt.show()

```

```py
plt.hist(x,bins=None, range=None, density=False, 
weights=None, cumulative=False, bottom=None, 
histtype='bar', align='mid', orientation='vertical', 
rwidth=None, log=False, color=None, 
label=None, stacked=False, **kwargs)
```


`x` : 데이터값으로 리스트나 ndarray 타입으로 넣음
`bins` : 막대(bins)의 갯수
`range` : bin의 범위
`density` : Normalize를 함.
`weights` : x에 있는 모든 값의 가중치(y값의미) , x와 모양 같아야함
`cumulative` :  True면 누적그래프로 그림
`histtype` : 막대그래프 타입(bar, barstacked, step, stepfilled)
`align` : 각 막대 중앙의 위치(left, mid, right)
`orientation` : 막대의 방향(vertical,horizontal), 기본값은 vertical, horizontal으로 하면 가로타입 막대그래프로 그려짐
`rwith` : 막대의 너비
`log` : True면 히스토그램의 축이 로그 스케일(log scale)로 결정
`color` : 막대의 색 결정
`label` : 각 데이터에 대한 라벨을 붙임. 문자열이고 2개이상일때는 리스트로 넣어야함
`stacked` : True일때 다수의 데이터를 겹쳐서 표현할 수 있음. 

