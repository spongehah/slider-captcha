(function () {
    'use strict';

    var extend = function () {
        var length = arguments.length;
        var target = arguments[0] || {};
        if (typeof target != "object" && typeof target != "function") {
            target = {};
        }
        if (length == 1) {
            target = this;
            i--;
        }
        for (var i = 1; i < length; i++) {
            var source = arguments[i];
            for (var key in source) {
                // 使用for in会遍历数组所有的可枚举属性，包括原型。
                if (Object.prototype.hasOwnProperty.call(source, key)) {
                    target[key] = source[key];
                }
            }
        }
        return target;
    }

    var isFunction = function isFunction(obj) {
        return typeof obj === "function" && typeof obj.nodeType !== "number";
    };

    var SliderCaptcha = function (element, options) {
        this.$element = element;
        this.options = extend({}, SliderCaptcha.DEFAULTS, options);
        this.$element.style.position = 'relative';

        //获取屏幕宽度，自适应滑块验证码画布的宽度
        var screenWidth = screen.width;

        this.options.width = screenWidth;
        this.options.height = screenWidth / 1.866;

        this.$element.style.width = this.options.width + 'px';
        this.$element.style.height = this.options.height + 'px';
        this.$element.style.margin = '0 auto';
        this.initSliderCaptcha();
    };

    SliderCaptcha.VERSION = '1.0';
    SliderCaptcha.Author = 'spongehah@163.com';
    SliderCaptcha.DEFAULTS = {
        width: 280,     // canvas宽度
        height: 155,    // canvas高度
        PI: Math.PI,
        sliderL: 42,    // 滑块边长
        sliderR: 9,     // 滑块半径
        offset: 5,      // 容错偏差
        loadingText: '正在加载中...',
        failedText: '再试一次',
        barText: '向右滑动填充拼图',
        repeatIcon: 'fa fa-repeat',
        maxLoadCount: 3,
        remoteUrl: null,
        localImages: function () {
            return 'images/Pic' + Math.round(Math.random() * 4) + '.jpg';
        },
        verify: function (arr, url) {
            var ret = false;
            $.ajax({
                url: url,
                data: {
                    "datas": JSON.stringify(arr),
                },
                dataType: "json",
                type: "post",
                async: false,
                success: function (result) {
                    ret = JSON.stringify(result);
                    console.log("返回结果：" + ret)
                }
            });
            return ret;
        },
    };

    function Plugin(option) {
        var $this = document.getElementById(option.id);
        var options = typeof option === 'object' && option;
        return new SliderCaptcha($this, options);
    }

    window.sliderCaptcha = Plugin;
    window.sliderCaptcha.Constructor = SliderCaptcha;

    var _proto = SliderCaptcha.prototype;
    _proto.initSliderCaptcha = function () {
        var that = this;
        var data;
        $.ajax({
            url: this.options.remoteUrl + '/getSliderCaptcha',
            type: 'GET',
            async: false,
            cache: false,
            data: {
                width: Math.floor(that.options.width),
                height: Math.floor(that.options.height)
            },
            success: function (res) {
                data = res;
            },
            error: function () {
                console.error('Failed to fetch slider captcha data');
            }
        });
        that.init(data);
    };

    _proto.init = function (data) {
        this.bgImage = data.bgImage;
        this.slideImage = data.slideImage;

        this.initDOM();
        this.initImg();
        this.bindEvents();
    };

    _proto.initDOM = function () {
        var createElement = function (tagName, className) {
            var elment = document.createElement(tagName);
            elment.className = className;
            return elment;
        };

        var createCanvas = function (width, height) {
            var canvas = document.createElement('canvas');
            canvas.width = width;
            canvas.height = height;
            return canvas;
        };

        var canvas = createCanvas(this.options.width, this.options.height); // 画布
        var block = canvas.cloneNode(true); // 滑块
        var sliderContainer = createElement('div', 'sliderContainer');
        var refreshIcon = createElement('i', 'refreshIcon ' + this.options.repeatIcon);
        var sliderMask = createElement('div', 'sliderMask');
        var sliderbg = createElement('div', 'sliderbg');
        var slider = createElement('div', 'slider');
        var sliderIcon = createElement('i', 'fa fa-arrow-right sliderIcon');
        var text = createElement('span', 'sliderText');

        block.className = 'block';
        text.innerHTML = this.options.barText;

        var el = this.$element;
        el.appendChild(canvas);
        el.appendChild(refreshIcon);
        el.appendChild(block);
        slider.appendChild(sliderIcon);
        sliderMask.appendChild(slider);
        sliderContainer.appendChild(sliderbg);
        sliderContainer.appendChild(sliderMask);
        sliderContainer.appendChild(text);
        el.appendChild(sliderContainer);

        var _canvas = {
            canvas: canvas,
            block: block,
            sliderContainer: sliderContainer,
            refreshIcon: refreshIcon,
            slider: slider,
            sliderMask: sliderMask,
            sliderIcon: sliderIcon,
            text: text,
            canvasCtx: canvas.getContext('2d'),
            blockCtx: block.getContext('2d')
        };

        if (isFunction(Object.assign)) {
            Object.assign(this, _canvas);
        } else {
            extend(this, _canvas);
        }
    };

    _proto.initImg = function () {
        // 加载背景图片
        var that = this;
        var bgImage = new Image();
        bgImage.src = "data:image/png;base64," + this.bgImage;
        bgImage.onload = () => {
            this.canvasCtx.drawImage(bgImage, 0, 0, this.options.width, this.options.height);
        };

        // 加载滑块图片
        var blockImage = new Image();
        blockImage.src = "data:image/png;base64," + this.slideImage;
        blockImage.onload = () => {
            this.blockCtx.drawImage(blockImage, 0, 0);
            that.text.textContent = that.text.getAttribute('data-text');
        };
    };

    _proto.clean = function () {
        this.canvasCtx.clearRect(0, 0, this.options.width, this.options.height);
        this.blockCtx.clearRect(0, 0, this.options.width, this.options.height);
        this.block.width = this.options.width;
    };

    _proto.bindEvents = function () {
        var that = this;
        this.$element.addEventListener('selectstart', function () {
            return false;
        });

        this.refreshIcon.addEventListener('click', function () {
            that.text.textContent = that.options.barText;
            that.reset();
            if (isFunction(that.options.onRefresh)) that.options.onRefresh.call(that.$element);
        });

        var originX, originY, trail = [], startTime,
            isMouseDown = false;

        var handleDragStart = function (e) {
            if (that.text.classList.contains('text-danger')) return;
            originX = e.clientX || e.touches[0].clientX;
            originY = e.clientY || e.touches[0].clientY;
            startTime = new Date().getTime();
            isMouseDown = true;
        };

        var handleDragMove = function (e) {
            if (!isMouseDown) return false;
            var eventX = e.clientX || e.touches[0].clientX;
            var eventY = e.clientY || e.touches[0].clientY;
            var moveX = eventX - originX;
            var moveY = eventY - originY;
            if (moveX < 0 || moveX + 40 > that.options.width) return false;
            that.slider.style.left = (moveX - 1) + 'px';
            var blockLeft = (that.options.width - 40 - 20) / (that.options.width - 40) * moveX;
            that.block.style.left = blockLeft + 'px';

            that.sliderContainer.classList.add('sliderContainer_active');
            that.sliderMask.style.width = (moveX + 4) + 'px';
            trail.push(Math.round(moveY));
        };

        var handleDragEnd = function (e) {
            if (!isMouseDown) return false;
            isMouseDown = false;
            var eventX = e.clientX || e.changedTouches[0].clientX;
            if (eventX === originX) return false;
            that.sliderContainer.classList.remove('sliderContainer_active');
            that.trail = trail;
            that.startTime = startTime;
            var flag = that.verify();
            if (flag) {
                that.sliderContainer.classList.add('sliderContainer_success');
                if (isFunction(that.options.onSuccess)) that.options.onSuccess.call(that.$element);
            } else {
                that.sliderContainer.classList.add('sliderContainer_fail');
                if (isFunction(that.options.onFail)) that.options.onFail.call(that.$element);
                trail = [];
                setTimeout(function () {
                    that.text.innerHTML = that.options.failedText;
                    that.reset();
                }, 1000);
            }
        };

        this.slider.addEventListener('mousedown', handleDragStart);
        this.slider.addEventListener('touchstart', handleDragStart);
        document.addEventListener('mousemove', handleDragMove);
        document.addEventListener('touchmove', handleDragMove);
        document.addEventListener('mouseup', handleDragEnd);
        document.addEventListener('touchend', handleDragEnd);

        document.addEventListener('mousedown', function () {
            return false;
        });
        document.addEventListener('touchstart', function () {
            return false;
        });
        document.addEventListener('swipe', function () {
            return false;
        });
    };

    _proto.verify = function () {
        var that = this;
        var arr = this.trail;
        var endTime = new Date().getTime();
        var left = parseInt(this.block.style.left);

        //构建请求参数
        // var params = {
        //     startTime: that.startTime,
        //     endTime: endTime,
        //     left: left,
        //     trail: arr
        // };

        if (this.options.remoteUrl !== null) {
            return this.options.verify(JSON.stringify(that.startTime), JSON.stringify(endTime), JSON.stringify(left), JSON.stringify(arr), this.options.remoteUrl);
        } else {
            // var sum = function (x, y) { return x + y; };
            // var square = function (x) { return x * x; };
            // var average = arr.reduce(sum) / arr.length;
            // var deviations = arr.map(function (x) { return x - average; });
            // var stddev = Math.sqrt(deviations.map(square).reduce(sum) / arr.length);
            // verified = stddev !== 0;
            console.error("remoteUrl为null");
            return false;
        }
    };

    _proto.reset = function () {
        this.sliderContainer.classList.remove('sliderContainer_fail');
        this.sliderContainer.classList.remove('sliderContainer_success');
        this.slider.style.left = 0;
        this.block.style.left = 0;
        this.sliderMask.style.width = 0;
        this.clean();
        this.text.setAttribute('data-text', this.text.textContent);
        this.text.textContent = this.options.loadingText;

        // 重新加载当前页面
        window.location.reload();
    };
})();
