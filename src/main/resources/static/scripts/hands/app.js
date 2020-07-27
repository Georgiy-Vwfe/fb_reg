/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

	'use strict';

	// shared

	var _sharedOptions = __webpack_require__(1);

	var _sharedOptions2 = babelHelpers.interopRequireDefault(_sharedOptions);

	var _eventEmitter = __webpack_require__(2);

	var _eventEmitter2 = babelHelpers.interopRequireDefault(_eventEmitter);

	// common

	var _commonCommon = __webpack_require__(3);

	var _commonCommon2 = babelHelpers.interopRequireDefault(_commonCommon);

	// molecules

	var _blocksMoleculesForm_Form = __webpack_require__(4);

	var _blocksMoleculesForm_Form2 = babelHelpers.interopRequireDefault(_blocksMoleculesForm_Form);

	// atoms

	var _blocksAtomsFormGroup_FormGroup = __webpack_require__(5);

	var _blocksAtomsFormGroup_FormGroup2 = babelHelpers.interopRequireDefault(_blocksAtomsFormGroup_FormGroup);

	var _blocksAtomsTooltip_Tooltip = __webpack_require__(6);

	var _blocksAtomsTooltip_Tooltip2 = babelHelpers.interopRequireDefault(_blocksAtomsTooltip_Tooltip);

	$(function () {
	  /* # Options (GLOBAL) */
	  window.opt = new _sharedOptions2['default']();
	  window.opt.init();

	  /* # Event emitter (GLOBAL) */
	  window.ee = new _eventEmitter2['default']();

	  /* # page - имя текущей страницы. Метка для вызова определенных классов/функций */
	  if (!window.opt.dataProvider.page) {
	    window.opt.dataProvider.page = 'index';
	  }

	  /* # Common methods */
	  (0, _commonCommon2['default'])();

	  /* # Set touch/no-touch */
	  if (window.opt.isMobile === true) {
	    $('html').addClass('touch');
	  } else {
	    $('html').addClass('no-touch');
	  }

	  /* # form */
	  var form = new _blocksMoleculesForm_Form2['default']();
	  form.init();

	  /* # tooltip */
	  var formGroup = new _blocksAtomsFormGroup_FormGroup2['default']();
	  formGroup.init();

	  /* # tooltip */
	  var tooltip = new _blocksAtomsTooltip_Tooltip2['default']();
	  tooltip.init();
	});

/***/ }),
/* 1 */
/***/ (function(module, exports) {

	'use strict';

	Object.defineProperty(exports, '__esModule', {
	  value: true
	});

	var Options = (function () {
	  // Set language and country data

	  function Options() {
	    babelHelpers.classCallCheck(this, Options);

	    var klasses = document.documentElement.className;
	    var langIndx = klasses.indexOf('lang-');
	    var countryIndx = klasses.indexOf('country-');

	    this.lang = null;
	    this.country = null;

	    if (langIndx >= 0) {
	      this.lang = klasses.substr(langIndx + 5, 2);
	    }

	    if (countryIndx >= 0) {
	      this.country = klasses.substr(countryIndx + 8, 2);
	    }
	  }

	  babelHelpers.createClass(Options, [{
	    key: 'init',
	    value: function init() {
	      this.agent();
	      this.storage();
	      this.phone();

	      this.dataProvider = {};
	      this.setDataProvider();
	    }

	    // Browser, mobile, android, etc.
	  }, {
	    key: 'agent',
	    value: function agent() {
	      this.ie8 = $('html').is('.ie8');
	      this.agent = navigator.userAgent.toLowerCase();
	      this.mobile = /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(this.agent);
	      this.android = /android/i.test(this.agent);
	    }

	    // Seesion and local storage
	  }, {
	    key: 'storage',
	    value: function storage() {
	      var storageAvailable = function storageAvailable(type) {
	        try {
	          var storage = window[type];
	          var x = '__storage_test__';
	          storage.setItem(x, x);
	          storage.removeItem(x);
	          return true;
	        } catch (e) {
	          return false;
	        }
	      };

	      this.sessionStorage = false;
	      this.localStorage = false;

	      if (storageAvailable('sessionStorage')) {
	        this.sessionStorage = true;
	      }

	      if (storageAvailable('localStorage')) {
	        this.localStorage = true;
	      }
	    }

	    // Local phone number mask and regular expression
	  }, {
	    key: 'phone',
	    value: function phone() {
	      switch (this.country) {
	        case 'ru':
	          this.phoneMask = ['+7 (999) 999-99-99'];
	          this.phoneRegexp = /^(\+?7)?(\+?8)? ?\(?(\d{3})\)? ?(\d{3})[ -]?(\d{2})[ -]?(\d{2})$/;
	          break;
	        case 'vn':
	          this.phoneMask = ['+99-99-9999-999', '+99 (999) 9999-999'];
	          this.phoneRegexp = /^(\+?84)?[ -]?\(?(\d{2,3})\)?[ -]?(\d{4})[ -]?(\d{3})$/;
	          break;
	        case 'th':
	          this.phoneMask = ['+99-99-999-9999', '+99 (999) 999-9999'];
	          this.phoneRegexp = /^(\+?66)?[ -]?\(?(\d{2,3})\)?[ -]?(\d{3})[ -]?(\d{4})$/;
	          break;
	        case 'id':
	          this.phoneMask = ['+62(899)999-9999', '+62-99-999-99', '+62-99-999-999', '+62-99-999-9999', '+62(899)999-999', '+62(899)999-99-999'];
	          this.phoneRegexp = /^(\+?62)?[ -]?\(?(\d{2,3})\)?[ -]?(\d{2,3})[ -]?(\d{2,4})[ -]?(\d{3,4})?$/;
	          break;
	        case 'ph':
	          this.phoneMask = ['+63-999-999-9999'];
	          this.phoneRegexp = /^(\+?63)?[ -]?\(?(\d{3})\)?[ -]?(\d{3})[ -]?(\d{4})?$/;
	          break;
	        default:
	          this.phoneMask = [];
	          this.phoneRegexp = '';
	      }
	    }

	    // Set additional data from the [data-provider] tag
	  }, {
	    key: 'setDataProvider',
	    value: function setDataProvider() {
	      var _this = this;

	      $('.dataProvider').each(function (i, el) {
	        $.extend(true, _this.dataProvider, $(el).data());
	      });
	      $('.dataProvider').remove();
	    }
	  }]);
	  return Options;
	})();

	exports['default'] = Options;
	module.exports = exports['default'];

/***/ }),
/* 2 */
/***/ (function(module, exports) {

	"use strict";

	Object.defineProperty(exports, "__esModule", {
	  value: true
	});

	var EventEmitter = (function () {
	  function EventEmitter() {
	    babelHelpers.classCallCheck(this, EventEmitter);

	    this.listeners = [];
	  }

	  babelHelpers.createClass(EventEmitter, [{
	    key: "addListener",
	    value: function addListener(label, callback) {
	      if (!this.listeners[label]) {
	        this.listeners[label] = [];
	      }
	      return this.listeners[label].push(callback);
	    }
	  }, {
	    key: "emit",
	    value: function emit(label) {
	      for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
	        args[_key - 1] = arguments[_key];
	      }

	      var listeners = this.listeners[label];

	      if (listeners && listeners.length) {
	        listeners.forEach(function (listener) {
	          listener.apply(undefined, args);
	        });
	        return true;
	      }
	      return false;
	    }
	  }]);
	  return EventEmitter;
	})();

	exports["default"] = EventEmitter;
	module.exports = exports["default"];

/***/ }),
/* 3 */
/***/ (function(module, exports) {

	'use strict';

	Object.defineProperty(exports, '__esModule', {
	  value: true
	});
	exports['default'] = common;

	function common() {
	  // Scroll to element
	  $(document).on('click', '[data-scroll-to]', function (e) {
	    e.preventDefault();
	    var el = $(e.target).is('[data-scroll-to]') ? $(e.target) : $(e.target).closest('[data-scroll-to]');
	    var domTarget = $('*[data-scroll-to-target="' + el.data('scroll-to') + '"]');

	    if (domTarget.length) {
	      $('html, body').animate({ scrollTop: domTarget.offset().top }, 600);
	    }
	  });

	  // Collapse
	  $(document).on('click', '[data-collapse]', function (e) {
	    e.preventDefault();
	    var el = $(e.target).is('[data-collapse]') ? $(e.target) : $(e.target).closest('[data-collapse]');
	    var domTarget = $('*[data-collapse-target="' + el.data('collapse') + '"]');

	    if (domTarget.length) {
	      el.toggleClass('active');
	      domTarget.toggleClass('show');
	    }
	  });
	}

	module.exports = exports['default'];

/***/ }),
/* 4 */
/***/ (function(module, exports) {

	'use strict';

	Object.defineProperty(exports, '__esModule', {
	  value: true
	});

	var Form = (function () {
	  function Form() {
	    babelHelpers.classCallCheck(this, Form);

	    this.text = 'class Form';
	  }

	  babelHelpers.createClass(Form, [{
	    key: 'init',
	    value: function init() {
	      console.log(this.text);
	    }
	  }]);
	  return Form;
	})();

	exports['default'] = Form;
	module.exports = exports['default'];

/***/ }),
/* 5 */
/***/ (function(module, exports) {

	'use strict';

	Object.defineProperty(exports, '__esModule', {
	  value: true
	});

	var FormGroup = (function () {
	  function FormGroup() {
	    babelHelpers.classCallCheck(this, FormGroup);

	    this.el = $('.form-group__field');
	    this.focusClass = 'focus';
	  }

	  babelHelpers.createClass(FormGroup, [{
	    key: 'init',
	    value: function init() {
	      this.el.on('focus', function () {
	        $(this).closest('.form-group').addClass('focus');
	      });

	      this.el.on('blur', function () {
	        // if the input is empty, delete the focus class
	        if ($(this).val() === '') {
	          $(this).closest('.form-group').removeClass('focus');
	        }
	      });
	    }
	  }]);
	  return FormGroup;
	})();

	exports['default'] = FormGroup;
	module.exports = exports['default'];

/***/ }),
/* 6 */
/***/ (function(module, exports) {

	'use strict';

	Object.defineProperty(exports, '__esModule', {
	  value: true
	});

	var Tooltip = (function () {
	  function Tooltip() {
	    babelHelpers.classCallCheck(this, Tooltip);
	  }

	  babelHelpers.createClass(Tooltip, [{
	    key: 'init',
	    value: function init() {
	      // Vertical center position
	      function verticalPosition() {
	        $('.tooltip.right, .tooltip.left').each(function () {
	          var outerHeight = $(this).outerHeight();
	          var halfHeight = outerHeight / 2;
	          $(this).css('margin-top', -halfHeight);
	        });
	      }
	      verticalPosition();

	      // Gorizontal center position
	      function gorizontalPosition() {
	        $('.tooltip.top, .tooltip.bottom').each(function () {
	          var outerWidth = $(this).outerWidth();
	          var halfWidth = outerWidth / 2;
	          $(this).css('margin-left', -halfWidth);
	        });
	      }
	      gorizontalPosition();

	      // Recalculate position after resize
	      $(window).resize(function () {
	        verticalPosition();
	        gorizontalPosition();
	      });
	    }
	  }]);
	  return Tooltip;
	})();

	exports['default'] = Tooltip;
	module.exports = exports['default'];

/***/ })
/******/ ]);