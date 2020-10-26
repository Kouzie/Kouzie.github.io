---
title:  "Nodejs - class!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Nodejs

tags:
  - Nodejs

toc: true
toc_sticky: true

---

## class

```js
class Square {
    constructor(height, width) {
        this.height = height;
        this.width = width;
    }
    get area() { // get keyword 사용시 메소드를 속성처럼 사용 가능
        return this.calcArea();
    }
    calcArea() {
        return this.height * this.width;
    }
}

var s1 = new Square(1, 2);
var s2 = new Square(1, 22);

console.log(typeof s1.area); // 2
console.log(typeof s2.area); // 22
```