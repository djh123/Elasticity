# Elasticity

本弹性框架是参考 facebook 的rebound 扩展开发的.如有侵权亲通知邮箱1318776407@qq.com

主要是扩展成可以动态添加自己的弹性或者其他动画表达式.
扩张本意,是为了对接UI的AE做图工具,如果UI在AE中使用了某种公式的动画效果,程序员可跟据本框架添加相应的算法.
这样就可以做到UI和程序员对接的时候只需要传算法参数而不需要一大堆的文字描述语言.



比如:本人第一次做弹性动画的时候UI出了个AE图  然后跟我说 在Y轴 500ms 位移 500px 后来回弹三次.

当时就用 TranslateAnimation 一直调都达不到UI所要的效果包括加入了 BounceInterpolator 进行颤斗.
后面就使用了 facebook rebound 但是参数调来调去 总是感觉差那么一点.
后面就发先了 他们AE的万能弹性公式 如下

```
amp = .1; 
freq = 2.0; 
decay = 2.0; 
n = 0; 
if (numKeys > 0){ 
 n = nearestKey(time).index; 
 if (key(n).time > time){n--;} 
  } 
if (n == 0){ t = 0;} 
else{t = time - key(n).time;} 
if (n > 0){ 
 v = velocityAtTime(key(n).time - thisComp.frameDuration/10); 
 value + v*amp*Math.sin(freq*t*2*Math.PI)/Math.exp(decay*t); 
   } 
else{value} 

```


后来我就想做个弹性公式和它一样的就一样了. 然后UI只需要给 amp = .1; freq = 2.0; decay = 2.0  这三个参数就行了,想法非常好.但是做起来是有好多问题的包括现在还有很多问题还没解决,
但好在公司很多人支持我慢慢去改善.

想到后面可能有很多类似的公式或者算法等,于是就借用了 facebook rebound 的框架,往上抽成统一的类,往下写成独立算法类.就可以实时的添加算法.




