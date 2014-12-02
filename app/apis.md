获取分类：
================
GET:http://www.guokr.com/apis/favorite/category.json?access_token=13d4b2b74c323e79b5e35b6c46fd8d9e2e3f7b937ecbca9dd9e39943cc4a7044

Accept: application/json, text/javascript, */*; q=0.01
X-Requested-With: XMLHttpRequest

Http Code:
> 200 ok

获取果篮
================
GET:http://www.guokr.com/apis/favorite/basket.json?t=1416970098312&retrieve_type=by_ukey&ukey=5p6t9t&limit=100&access_token=5fe191c401d8a1d646cfe3cb3f23cac6e400f42d3ed2b82ecbd3a5d6508a36a5

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:post_id=646116&access_token=f182c94e82887148a7208e242f68ae9e2f81baf2af1bbed796e405e24d1d12f1

Http Code:
> 200 ok


创建果篮
================
POST http://www.guokr.com/apis/favorite/basket.json

Host: www.guokr.com
Accept: application/json, text/javascript, */*; q=0.01
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:(UrlEncode)title=中文Test&introduction=中文Desciption&category_id=13&access_token=13d4b2b74c323e79b5e35b6c46fd8d9e2e3f7b937ecbca9dd9e39943cc4a7044
category_id=-1指无分类

Http Code:
> 200 ok


---------------------------------------------------------------
上传图片
===============
POST:http://www.guokr.com/apis/image.json?enable_watermark=true

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary2O2cnCklDGYBEGNm

Params:`access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157&upload_file=<file>`

Http Code:
> 201 ok



-------------------------------------------------------------
收藏一个帖子
================
POST:http://www.guokr.com/apis/favorite/link.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:(UrlEncode)basket_id=786415&url=http://www.guokr.com/post/646133/&title=［深入绝地］告别LOL、dota2，周末和小伙伴一起刷桌游吧～&access_token=5fe191c401d8a1d646cfe3cb3f23cac6e400f42d3ed2b82ecbd3a5d6508a36a5

Http Code:
> 201 ok(Favor unlimited)


-------------------------------------------------------------
赞一个帖子
================
POST:http://www.guokr.com/apis/group/post_liking.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:post_id=646116&access_token=f182c94e82887148a7208e242f68ae9e2f81baf2af1bbed796e405e24d1d12f1

Http Code:
201 ok
400 already liked


---------------------------------------------------------------
回复帖子
===============
POST:http://apis.guokr.com/group/post_reply.json

Content-Type: Content-Type: application/x-www-form-urlencoded

Params:`post_id=#&content=#&access_token=#`

Http Code:
> 201 ok

-------------------------------------------------------------
赞一个帖子评论
===============
POST:http://www.guokr.com/apis/group/post_reply_liking.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

params:reply_id=5842479&access_token=f182c94e82887148a7208e242f68ae9e2f81baf2af1bbed796e405e24d1d12f1

Http Code:
201 ok
400 already liked


-------------------------------------------------------------
收藏文章
================
POST:http://www.guokr.com/apis/favorite/link.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:(UrlEncode))`basket_id=786415&url=http://www.guokr.com/article/439556/&title= 总是吃太多，可能是盘子惹的祸&access_token=5fe191c401d8a1d646cfe3cb3f23cac6e400f42d3ed2b82ecbd3a5d6508a36a5`

Http Code:
> 201 ok(Favor unlimited)


-------------------------------------------------------------
推荐文章
================
POST:http://www.guokr.com/apis/community/user/recommend.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:(UrlEncode))`title=小小粉尘，何以酿成大祸？&url=http://www.guokr.com/article/438909/&image=http://3.im.guokr.com/KP7FvVVlEk9LZ4N68R3tcVZeqzJoxTyybFr4fDNwby9KAQAA7QAAAEpQ.jpg?imageView2/1/w/166/h/129&summary=昆山爆炸事件一时间牵动着亿万人的心，罪魁祸首很快被锁定在防护不当引起的金属粉尘引爆炸。粉尘的危害不仅如此，长期反复接触粉尘还会造成严重的肺部慢性损伤尘肺。粉尘污染，已然是危害产业工人健康的最危险因素。&comment=&target=activity&access_token=5fe191c401d8a1d646cfe3cb3f23cac6e400f42d3ed2b82ecbd3a5d6508a36a5`

Http Code:
> 201 ok(recommend unlimited)


---------------------------------------------------------------
回复文章
===============
POST:http://apis.guokr.com/minisite/article_reply.json

Content-Type: application/x-www-form-urlencoded

Params:`article_id=439564&content=#&access_token=#`

Http Code:
> 201 ok

-------------------------------------------------------------
赞文章评论
================
POST:http://www.guokr.com/apis/minisite/article_reply_liking.json

Host: www.guokr.com
Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8


Params:`reply_id=2840882&access_token=5fe191c401d8a1d646cfe3cb3f23cac6e400f42d3ed2b82ecbd3a5d6508a36a5`

Http Code:
> 201 ok
> 400 already liked



-------------------------------------------------------------
收藏问题
================
Get:http://www.guokr.com/apis/favorite/basket.json?t=1416970377761&retrieve_type=by_ukey&ukey=5p6t9t&limit=100&access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Http Code:
> 201 ok(Favor unlimited)


-------------------------------------------------------------
推荐问题
================
POST:http://www.guokr.com/apis/community/user/recommend.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:(UrlEncode))`title=火有影子吗？&summary=Quora上有人问过这个问题 http://www.quora.com/Why-does-fire-or-a-flame-not-make-shadow-while-other-things-around-it-do不知道对不对。...&url=http://www.guokr.com/question/585042/&target=activity&comment=&access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157`

Http Code:

> 201 ok(recommend unlimited)


---------------------------------------------------------------
回答问题
===============
POST:http://apis.guokr.com/ask/answer.json

Content-Type: application/x-www-form-urlencoded

Params:`question_id=#&content=#&access_token=#`

Http Code:
> 201 ok

-------------------------------------------------------------
顶踩答案
================
POST:http://www.guokr.com/apis/ask/answer_polling.json

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Support Params:`answer_id=761921&opinion=support&access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157`
Against Params:`answer_id=761921&opinion=oppose&access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157`

Http Code:
> 201 ok(Support/Against unlimited)


-------------------------------------------------------------
感谢答案
================
POST:http://www.guokr.com/apis/ask/answer_thanking.json?v=1416970848197(this may be a random)

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:answer_id=761921&access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157

Http Code:
> 201 ok
> 400 already liked


-------------------------------------------------------------
不是答案
================
POST:http://www.guokr.com/apis/ask/answer_burying.json(this may be a random)

Host: www.guokr.com
X-Requested-With: XMLHttpRequest
Content-Type: application/x-www-form-urlencoded; charset=UTF-8

Params:`answer_id=761928&access_token=369a930a7c06f2ce10a838e45fc21da1e80ac81d458244cc78110e17714af157`

Http Code:
> 201 ok
> 400 already buried