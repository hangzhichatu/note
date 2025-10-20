define("DS/W3DPassport/dsp/utils/areRequiredOptionsMissing", [], function () {
  "use strict";
  return function (e, t) {
    var n,
      i = !1;
    for (n = t.length; !i && 0 < n; ) (n -= 1), void 0 === e[t[n]] && (i = !0);
    return i;
  };
});
var CryptoJS =
  CryptoJS ||
  (function (e, t) {
    var n = {},
      i = (n.lib = {}),
      r = function () {},
      a = (i.Base = {
        extend: function (e) {
          r.prototype = this;
          var t = new r();
          return (
            e && t.mixIn(e),
            t.hasOwnProperty("init") ||
              (t.init = function () {
                t.$super.init.apply(this, arguments);
              }),
            (t.init.prototype = t),
            (t.$super = this),
            t
          );
        },
        create: function () {
          var e = this.extend();
          return e.init.apply(e, arguments), e;
        },
        init: function () {},
        mixIn: function (e) {
          for (var t in e) e.hasOwnProperty(t) && (this[t] = e[t]);
          e.hasOwnProperty("toString") && (this.toString = e.toString);
        },
        clone: function () {
          return this.init.prototype.extend(this);
        },
      }),
      o = (i.WordArray = a.extend({
        init: function (e, t) {
          (e = this.words = e || []),
            (this.sigBytes = void 0 != t ? t : 4 * e.length);
        },
        toString: function (e) {
          return (e || l).stringify(this);
        },
        concat: function (e) {
          var t = this.words,
            n = e.words,
            i = this.sigBytes;
          if (((e = e.sigBytes), this.clamp(), i % 4))
            for (var r = 0; r < e; r++)
              t[(i + r) >>> 2] |=
                ((n[r >>> 2] >>> (24 - (r % 4) * 8)) & 255) <<
                (24 - ((i + r) % 4) * 8);
          else if (65535 < n.length)
            for (r = 0; r < e; r += 4) t[(i + r) >>> 2] = n[r >>> 2];
          else t.push.apply(t, n);
          return (this.sigBytes += e), this;
        },
        clamp: function () {
          var t = this.words,
            n = this.sigBytes;
          (t[n >>> 2] &= 4294967295 << (32 - (n % 4) * 8)),
            (t.length = e.ceil(n / 4));
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e.words = this.words.slice(0)), e;
        },
        random: function (t) {
          for (var n = [], i = 0; i < t; i += 4)
            n.push((4294967296 * e.random()) | 0);
          return new o.init(n, t);
        },
      })),
      s = (n.enc = {}),
      l = (s.Hex = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++) {
            var r = (t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255;
            n.push((r >>> 4).toString(16)), n.push((15 & r).toString(16));
          }
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i += 2)
            n[i >>> 3] |= parseInt(e.substr(i, 2), 16) << (24 - (i % 8) * 4);
          return new o.init(n, t / 2);
        },
      }),
      c = (s.Latin1 = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++)
            n.push(
              String.fromCharCode((t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255)
            );
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i++)
            n[i >>> 2] |= (255 & e.charCodeAt(i)) << (24 - (i % 4) * 8);
          return new o.init(n, t);
        },
      }),
      u = (s.Utf8 = {
        stringify: function (e) {
          try {
            return decodeURIComponent(escape(c.stringify(e)));
          } catch (e) {
            throw Error("Malformed UTF-8 data");
          }
        },
        parse: function (e) {
          return c.parse(unescape(encodeURIComponent(e)));
        },
      }),
      d = (i.BufferedBlockAlgorithm = a.extend({
        reset: function () {
          (this._data = new o.init()), (this._nDataBytes = 0);
        },
        _append: function (e) {
          "string" == typeof e && (e = u.parse(e)),
            this._data.concat(e),
            (this._nDataBytes += e.sigBytes);
        },
        _process: function (t) {
          var n = this._data,
            i = n.words,
            r = n.sigBytes,
            a = this.blockSize,
            s = r / (4 * a),
            s = t ? e.ceil(s) : e.max((0 | s) - this._minBufferSize, 0);
          if (((t = s * a), (r = e.min(4 * t, r)), t)) {
            for (var l = 0; l < t; l += a) this._doProcessBlock(i, l);
            (l = i.splice(0, t)), (n.sigBytes -= r);
          }
          return new o.init(l, r);
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e._data = this._data.clone()), e;
        },
        _minBufferSize: 0,
      }));
    i.Hasher = d.extend({
      cfg: a.extend(),
      init: function (e) {
        (this.cfg = this.cfg.extend(e)), this.reset();
      },
      reset: function () {
        d.reset.call(this), this._doReset();
      },
      update: function (e) {
        return this._append(e), this._process(), this;
      },
      finalize: function (e) {
        return e && this._append(e), this._doFinalize();
      },
      blockSize: 16,
      _createHelper: function (e) {
        return function (t, n) {
          return new e.init(n).finalize(t);
        };
      },
      _createHmacHelper: function (e) {
        return function (t, n) {
          return new p.HMAC.init(e, n).finalize(t);
        };
      },
    });
    var p = (n.algo = {});
    return n;
  })(Math);
!(function (e) {
  function t(e, t, n, i, r, a, o) {
    return (
      (((e = e + ((t & n) | (~t & i)) + r + o) << a) | (e >>> (32 - a))) + t
    );
  }
  function n(e, t, n, i, r, a, o) {
    return (
      (((e = e + ((t & i) | (n & ~i)) + r + o) << a) | (e >>> (32 - a))) + t
    );
  }
  function i(e, t, n, i, r, a, o) {
    return (((e = e + (t ^ n ^ i) + r + o) << a) | (e >>> (32 - a))) + t;
  }
  function r(e, t, n, i, r, a, o) {
    return (((e = e + (n ^ (t | ~i)) + r + o) << a) | (e >>> (32 - a))) + t;
  }
  for (
    var a = CryptoJS,
      o = a.lib,
      s = o.WordArray,
      l = o.Hasher,
      o = a.algo,
      c = [],
      u = 0;
    64 > u;
    u++
  )
    c[u] = (4294967296 * e.abs(e.sin(u + 1))) | 0;
  (o = o.MD5 =
    l.extend({
      _doReset: function () {
        this._hash = new s.init([
          1732584193, 4023233417, 2562383102, 271733878,
        ]);
      },
      _doProcessBlock: function (e, a) {
        for (var o = 0; 16 > o; o++) {
          var s = a + o,
            l = e[s];
          e[s] =
            (16711935 & ((l << 8) | (l >>> 24))) |
            (4278255360 & ((l << 24) | (l >>> 8)));
        }
        var o = this._hash.words,
          s = e[a + 0],
          l = e[a + 1],
          u = e[a + 2],
          d = e[a + 3],
          p = e[a + 4],
          f = e[a + 5],
          h = e[a + 6],
          m = e[a + 7],
          g = e[a + 8],
          v = e[a + 9],
          w = e[a + 10],
          y = e[a + 11],
          b = e[a + 12],
          E = e[a + 13],
          S = e[a + 14],
          D = e[a + 15],
          _ = o[0],
          x = o[1],
          C = o[2],
          k = o[3],
          _ = t(_, x, C, k, s, 7, c[0]),
          k = t(k, _, x, C, l, 12, c[1]),
          C = t(C, k, _, x, u, 17, c[2]),
          x = t(x, C, k, _, d, 22, c[3]),
          _ = t(_, x, C, k, p, 7, c[4]),
          k = t(k, _, x, C, f, 12, c[5]),
          C = t(C, k, _, x, h, 17, c[6]),
          x = t(x, C, k, _, m, 22, c[7]),
          _ = t(_, x, C, k, g, 7, c[8]),
          k = t(k, _, x, C, v, 12, c[9]),
          C = t(C, k, _, x, w, 17, c[10]),
          x = t(x, C, k, _, y, 22, c[11]),
          _ = t(_, x, C, k, b, 7, c[12]),
          k = t(k, _, x, C, E, 12, c[13]),
          C = t(C, k, _, x, S, 17, c[14]),
          x = t(x, C, k, _, D, 22, c[15]),
          _ = n(_, x, C, k, l, 5, c[16]),
          k = n(k, _, x, C, h, 9, c[17]),
          C = n(C, k, _, x, y, 14, c[18]),
          x = n(x, C, k, _, s, 20, c[19]),
          _ = n(_, x, C, k, f, 5, c[20]),
          k = n(k, _, x, C, w, 9, c[21]),
          C = n(C, k, _, x, D, 14, c[22]),
          x = n(x, C, k, _, p, 20, c[23]),
          _ = n(_, x, C, k, v, 5, c[24]),
          k = n(k, _, x, C, S, 9, c[25]),
          C = n(C, k, _, x, d, 14, c[26]),
          x = n(x, C, k, _, g, 20, c[27]),
          _ = n(_, x, C, k, E, 5, c[28]),
          k = n(k, _, x, C, u, 9, c[29]),
          C = n(C, k, _, x, m, 14, c[30]),
          x = n(x, C, k, _, b, 20, c[31]),
          _ = i(_, x, C, k, f, 4, c[32]),
          k = i(k, _, x, C, g, 11, c[33]),
          C = i(C, k, _, x, y, 16, c[34]),
          x = i(x, C, k, _, S, 23, c[35]),
          _ = i(_, x, C, k, l, 4, c[36]),
          k = i(k, _, x, C, p, 11, c[37]),
          C = i(C, k, _, x, m, 16, c[38]),
          x = i(x, C, k, _, w, 23, c[39]),
          _ = i(_, x, C, k, E, 4, c[40]),
          k = i(k, _, x, C, s, 11, c[41]),
          C = i(C, k, _, x, d, 16, c[42]),
          x = i(x, C, k, _, h, 23, c[43]),
          _ = i(_, x, C, k, v, 4, c[44]),
          k = i(k, _, x, C, b, 11, c[45]),
          C = i(C, k, _, x, D, 16, c[46]),
          x = i(x, C, k, _, u, 23, c[47]),
          _ = r(_, x, C, k, s, 6, c[48]),
          k = r(k, _, x, C, m, 10, c[49]),
          C = r(C, k, _, x, S, 15, c[50]),
          x = r(x, C, k, _, f, 21, c[51]),
          _ = r(_, x, C, k, b, 6, c[52]),
          k = r(k, _, x, C, d, 10, c[53]),
          C = r(C, k, _, x, w, 15, c[54]),
          x = r(x, C, k, _, l, 21, c[55]),
          _ = r(_, x, C, k, g, 6, c[56]),
          k = r(k, _, x, C, D, 10, c[57]),
          C = r(C, k, _, x, h, 15, c[58]),
          x = r(x, C, k, _, E, 21, c[59]),
          _ = r(_, x, C, k, p, 6, c[60]),
          k = r(k, _, x, C, y, 10, c[61]),
          C = r(C, k, _, x, u, 15, c[62]),
          x = r(x, C, k, _, v, 21, c[63]);
        (o[0] = (o[0] + _) | 0),
          (o[1] = (o[1] + x) | 0),
          (o[2] = (o[2] + C) | 0),
          (o[3] = (o[3] + k) | 0);
      },
      _doFinalize: function () {
        var t = this._data,
          n = t.words,
          i = 8 * this._nDataBytes,
          r = 8 * t.sigBytes;
        n[r >>> 5] |= 128 << (24 - (r % 32));
        var a = e.floor(i / 4294967296);
        for (
          n[15 + (((r + 64) >>> 9) << 4)] =
            (16711935 & ((a << 8) | (a >>> 24))) |
            (4278255360 & ((a << 24) | (a >>> 8))),
            n[14 + (((r + 64) >>> 9) << 4)] =
              (16711935 & ((i << 8) | (i >>> 24))) |
              (4278255360 & ((i << 24) | (i >>> 8))),
            t.sigBytes = 4 * (n.length + 1),
            this._process(),
            t = this._hash,
            n = t.words,
            i = 0;
          4 > i;
          i++
        )
          (r = n[i]),
            (n[i] =
              (16711935 & ((r << 8) | (r >>> 24))) |
              (4278255360 & ((r << 24) | (r >>> 8))));
        return t;
      },
      clone: function () {
        var e = l.clone.call(this);
        return (e._hash = this._hash.clone()), e;
      },
    })),
    (a.MD5 = l._createHelper(o)),
    (a.HmacMD5 = l._createHmacHelper(o));
})(Math),
  define("libs/cryptojs/md5", function () {});
var CryptoJS =
  CryptoJS ||
  (function (e, t) {
    var n = {},
      i = (n.lib = {}),
      r = function () {},
      a = (i.Base = {
        extend: function (e) {
          r.prototype = this;
          var t = new r();
          return (
            e && t.mixIn(e),
            t.hasOwnProperty("init") ||
              (t.init = function () {
                t.$super.init.apply(this, arguments);
              }),
            (t.init.prototype = t),
            (t.$super = this),
            t
          );
        },
        create: function () {
          var e = this.extend();
          return e.init.apply(e, arguments), e;
        },
        init: function () {},
        mixIn: function (e) {
          for (var t in e) e.hasOwnProperty(t) && (this[t] = e[t]);
          e.hasOwnProperty("toString") && (this.toString = e.toString);
        },
        clone: function () {
          return this.init.prototype.extend(this);
        },
      }),
      o = (i.WordArray = a.extend({
        init: function (e, t) {
          (e = this.words = e || []),
            (this.sigBytes = void 0 != t ? t : 4 * e.length);
        },
        toString: function (e) {
          return (e || l).stringify(this);
        },
        concat: function (e) {
          var t = this.words,
            n = e.words,
            i = this.sigBytes;
          if (((e = e.sigBytes), this.clamp(), i % 4))
            for (var r = 0; r < e; r++)
              t[(i + r) >>> 2] |=
                ((n[r >>> 2] >>> (24 - (r % 4) * 8)) & 255) <<
                (24 - ((i + r) % 4) * 8);
          else if (65535 < n.length)
            for (r = 0; r < e; r += 4) t[(i + r) >>> 2] = n[r >>> 2];
          else t.push.apply(t, n);
          return (this.sigBytes += e), this;
        },
        clamp: function () {
          var t = this.words,
            n = this.sigBytes;
          (t[n >>> 2] &= 4294967295 << (32 - (n % 4) * 8)),
            (t.length = e.ceil(n / 4));
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e.words = this.words.slice(0)), e;
        },
        random: function (t) {
          for (var n = [], i = 0; i < t; i += 4)
            n.push((4294967296 * e.random()) | 0);
          return new o.init(n, t);
        },
      })),
      s = (n.enc = {}),
      l = (s.Hex = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++) {
            var r = (t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255;
            n.push((r >>> 4).toString(16)), n.push((15 & r).toString(16));
          }
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i += 2)
            n[i >>> 3] |= parseInt(e.substr(i, 2), 16) << (24 - (i % 8) * 4);
          return new o.init(n, t / 2);
        },
      }),
      c = (s.Latin1 = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++)
            n.push(
              String.fromCharCode((t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255)
            );
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i++)
            n[i >>> 2] |= (255 & e.charCodeAt(i)) << (24 - (i % 4) * 8);
          return new o.init(n, t);
        },
      }),
      u = (s.Utf8 = {
        stringify: function (e) {
          try {
            return decodeURIComponent(escape(c.stringify(e)));
          } catch (e) {
            throw Error("Malformed UTF-8 data");
          }
        },
        parse: function (e) {
          return c.parse(unescape(encodeURIComponent(e)));
        },
      }),
      d = (i.BufferedBlockAlgorithm = a.extend({
        reset: function () {
          (this._data = new o.init()), (this._nDataBytes = 0);
        },
        _append: function (e) {
          "string" == typeof e && (e = u.parse(e)),
            this._data.concat(e),
            (this._nDataBytes += e.sigBytes);
        },
        _process: function (t) {
          var n = this._data,
            i = n.words,
            r = n.sigBytes,
            a = this.blockSize,
            s = r / (4 * a),
            s = t ? e.ceil(s) : e.max((0 | s) - this._minBufferSize, 0);
          if (((t = s * a), (r = e.min(4 * t, r)), t)) {
            for (var l = 0; l < t; l += a) this._doProcessBlock(i, l);
            (l = i.splice(0, t)), (n.sigBytes -= r);
          }
          return new o.init(l, r);
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e._data = this._data.clone()), e;
        },
        _minBufferSize: 0,
      }));
    i.Hasher = d.extend({
      cfg: a.extend(),
      init: function (e) {
        (this.cfg = this.cfg.extend(e)), this.reset();
      },
      reset: function () {
        d.reset.call(this), this._doReset();
      },
      update: function (e) {
        return this._append(e), this._process(), this;
      },
      finalize: function (e) {
        return e && this._append(e), this._doFinalize();
      },
      blockSize: 16,
      _createHelper: function (e) {
        return function (t, n) {
          return new e.init(n).finalize(t);
        };
      },
      _createHmacHelper: function (e) {
        return function (t, n) {
          return new p.HMAC.init(e, n).finalize(t);
        };
      },
    });
    var p = (n.algo = {});
    return n;
  })(Math);
!(function (e) {
  for (
    var t = CryptoJS,
      n = t.lib,
      i = n.WordArray,
      r = n.Hasher,
      n = t.algo,
      a = [],
      o = [],
      s = function (e) {
        return (4294967296 * (e - (0 | e))) | 0;
      },
      l = 2,
      c = 0;
    64 > c;

  ) {
    var u;
    e: {
      u = l;
      for (var d = e.sqrt(u), p = 2; p <= d; p++)
        if (!(u % p)) {
          u = !1;
          break e;
        }
      u = !0;
    }
    u && (8 > c && (a[c] = s(e.pow(l, 0.5))), (o[c] = s(e.pow(l, 1 / 3))), c++),
      l++;
  }
  var f = [],
    n = (n.SHA256 = r.extend({
      _doReset: function () {
        this._hash = new i.init(a.slice(0));
      },
      _doProcessBlock: function (e, t) {
        for (
          var n = this._hash.words,
            i = n[0],
            r = n[1],
            a = n[2],
            s = n[3],
            l = n[4],
            c = n[5],
            u = n[6],
            d = n[7],
            p = 0;
          64 > p;
          p++
        ) {
          if (16 > p) f[p] = 0 | e[t + p];
          else {
            var h = f[p - 15],
              m = f[p - 2];
            f[p] =
              (((h << 25) | (h >>> 7)) ^ ((h << 14) | (h >>> 18)) ^ (h >>> 3)) +
              f[p - 7] +
              (((m << 15) | (m >>> 17)) ^
                ((m << 13) | (m >>> 19)) ^
                (m >>> 10)) +
              f[p - 16];
          }
          (h =
            d +
            (((l << 26) | (l >>> 6)) ^
              ((l << 21) | (l >>> 11)) ^
              ((l << 7) | (l >>> 25))) +
            ((l & c) ^ (~l & u)) +
            o[p] +
            f[p]),
            (m =
              (((i << 30) | (i >>> 2)) ^
                ((i << 19) | (i >>> 13)) ^
                ((i << 10) | (i >>> 22))) +
              ((i & r) ^ (i & a) ^ (r & a))),
            (d = u),
            (u = c),
            (c = l),
            (l = (s + h) | 0),
            (s = a),
            (a = r),
            (r = i),
            (i = (h + m) | 0);
        }
        (n[0] = (n[0] + i) | 0),
          (n[1] = (n[1] + r) | 0),
          (n[2] = (n[2] + a) | 0),
          (n[3] = (n[3] + s) | 0),
          (n[4] = (n[4] + l) | 0),
          (n[5] = (n[5] + c) | 0),
          (n[6] = (n[6] + u) | 0),
          (n[7] = (n[7] + d) | 0);
      },
      _doFinalize: function () {
        var t = this._data,
          n = t.words,
          i = 8 * this._nDataBytes,
          r = 8 * t.sigBytes;
        return (
          (n[r >>> 5] |= 128 << (24 - (r % 32))),
          (n[14 + (((r + 64) >>> 9) << 4)] = e.floor(i / 4294967296)),
          (n[15 + (((r + 64) >>> 9) << 4)] = i),
          (t.sigBytes = 4 * n.length),
          this._process(),
          this._hash
        );
      },
      clone: function () {
        var e = r.clone.call(this);
        return (e._hash = this._hash.clone()), e;
      },
    }));
  (t.SHA256 = r._createHelper(n)), (t.HmacSHA256 = r._createHmacHelper(n));
})(Math),
  define("libs/cryptojs/sha256", function () {});
var CryptoJS =
  CryptoJS ||
  (function (e, t) {
    var n = {},
      i = (n.lib = {}),
      r = function () {},
      a = (i.Base = {
        extend: function (e) {
          r.prototype = this;
          var t = new r();
          return (
            e && t.mixIn(e),
            t.hasOwnProperty("init") ||
              (t.init = function () {
                t.$super.init.apply(this, arguments);
              }),
            (t.init.prototype = t),
            (t.$super = this),
            t
          );
        },
        create: function () {
          var e = this.extend();
          return e.init.apply(e, arguments), e;
        },
        init: function () {},
        mixIn: function (e) {
          for (var t in e) e.hasOwnProperty(t) && (this[t] = e[t]);
          e.hasOwnProperty("toString") && (this.toString = e.toString);
        },
        clone: function () {
          return this.init.prototype.extend(this);
        },
      }),
      o = (i.WordArray = a.extend({
        init: function (e, t) {
          (e = this.words = e || []),
            (this.sigBytes = void 0 != t ? t : 4 * e.length);
        },
        toString: function (e) {
          return (e || l).stringify(this);
        },
        concat: function (e) {
          var t = this.words,
            n = e.words,
            i = this.sigBytes;
          if (((e = e.sigBytes), this.clamp(), i % 4))
            for (var r = 0; r < e; r++)
              t[(i + r) >>> 2] |=
                ((n[r >>> 2] >>> (24 - (r % 4) * 8)) & 255) <<
                (24 - ((i + r) % 4) * 8);
          else if (65535 < n.length)
            for (r = 0; r < e; r += 4) t[(i + r) >>> 2] = n[r >>> 2];
          else t.push.apply(t, n);
          return (this.sigBytes += e), this;
        },
        clamp: function () {
          var t = this.words,
            n = this.sigBytes;
          (t[n >>> 2] &= 4294967295 << (32 - (n % 4) * 8)),
            (t.length = e.ceil(n / 4));
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e.words = this.words.slice(0)), e;
        },
        random: function (t) {
          for (var n = [], i = 0; i < t; i += 4)
            n.push((4294967296 * e.random()) | 0);
          return new o.init(n, t);
        },
      })),
      s = (n.enc = {}),
      l = (s.Hex = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++) {
            var r = (t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255;
            n.push((r >>> 4).toString(16)), n.push((15 & r).toString(16));
          }
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i += 2)
            n[i >>> 3] |= parseInt(e.substr(i, 2), 16) << (24 - (i % 8) * 4);
          return new o.init(n, t / 2);
        },
      }),
      c = (s.Latin1 = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++)
            n.push(
              String.fromCharCode((t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255)
            );
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i++)
            n[i >>> 2] |= (255 & e.charCodeAt(i)) << (24 - (i % 4) * 8);
          return new o.init(n, t);
        },
      }),
      u = (s.Utf8 = {
        stringify: function (e) {
          try {
            return decodeURIComponent(escape(c.stringify(e)));
          } catch (e) {
            throw Error("Malformed UTF-8 data");
          }
        },
        parse: function (e) {
          return c.parse(unescape(encodeURIComponent(e)));
        },
      }),
      d = (i.BufferedBlockAlgorithm = a.extend({
        reset: function () {
          (this._data = new o.init()), (this._nDataBytes = 0);
        },
        _append: function (e) {
          "string" == typeof e && (e = u.parse(e)),
            this._data.concat(e),
            (this._nDataBytes += e.sigBytes);
        },
        _process: function (t) {
          var n = this._data,
            i = n.words,
            r = n.sigBytes,
            a = this.blockSize,
            s = r / (4 * a),
            s = t ? e.ceil(s) : e.max((0 | s) - this._minBufferSize, 0);
          if (((t = s * a), (r = e.min(4 * t, r)), t)) {
            for (var l = 0; l < t; l += a) this._doProcessBlock(i, l);
            (l = i.splice(0, t)), (n.sigBytes -= r);
          }
          return new o.init(l, r);
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e._data = this._data.clone()), e;
        },
        _minBufferSize: 0,
      }));
    i.Hasher = d.extend({
      cfg: a.extend(),
      init: function (e) {
        (this.cfg = this.cfg.extend(e)), this.reset();
      },
      reset: function () {
        d.reset.call(this), this._doReset();
      },
      update: function (e) {
        return this._append(e), this._process(), this;
      },
      finalize: function (e) {
        return e && this._append(e), this._doFinalize();
      },
      blockSize: 16,
      _createHelper: function (e) {
        return function (t, n) {
          return new e.init(n).finalize(t);
        };
      },
      _createHmacHelper: function (e) {
        return function (t, n) {
          return new p.HMAC.init(e, n).finalize(t);
        };
      },
    });
    var p = (n.algo = {});
    return n;
  })(Math);
(function (e) {
  var t = CryptoJS,
    n = t.lib,
    i = n.Base,
    r = n.WordArray,
    t = (t.x64 = {});
  (t.Word = i.extend({
    init: function (e, t) {
      (this.high = e), (this.low = t);
    },
  })),
    (t.WordArray = i.extend({
      init: function (e, t) {
        (e = this.words = e || []),
          (this.sigBytes = void 0 != t ? t : 8 * e.length);
      },
      toX32: function () {
        for (var e = this.words, t = e.length, n = [], i = 0; i < t; i++) {
          var a = e[i];
          n.push(a.high), n.push(a.low);
        }
        return r.create(n, this.sigBytes);
      },
      clone: function () {
        for (
          var e = i.clone.call(this),
            t = (e.words = this.words.slice(0)),
            n = t.length,
            r = 0;
          r < n;
          r++
        )
          t[r] = t[r].clone();
        return e;
      },
    }));
})(),
  (function () {
    function e() {
      return r.create.apply(r, arguments);
    }
    for (
      var t = CryptoJS,
        n = t.lib.Hasher,
        i = t.x64,
        r = i.Word,
        a = i.WordArray,
        i = t.algo,
        o = [
          e(1116352408, 3609767458),
          e(1899447441, 602891725),
          e(3049323471, 3964484399),
          e(3921009573, 2173295548),
          e(961987163, 4081628472),
          e(1508970993, 3053834265),
          e(2453635748, 2937671579),
          e(2870763221, 3664609560),
          e(3624381080, 2734883394),
          e(310598401, 1164996542),
          e(607225278, 1323610764),
          e(1426881987, 3590304994),
          e(1925078388, 4068182383),
          e(2162078206, 991336113),
          e(2614888103, 633803317),
          e(3248222580, 3479774868),
          e(3835390401, 2666613458),
          e(4022224774, 944711139),
          e(264347078, 2341262773),
          e(604807628, 2007800933),
          e(770255983, 1495990901),
          e(1249150122, 1856431235),
          e(1555081692, 3175218132),
          e(1996064986, 2198950837),
          e(2554220882, 3999719339),
          e(2821834349, 766784016),
          e(2952996808, 2566594879),
          e(3210313671, 3203337956),
          e(3336571891, 1034457026),
          e(3584528711, 2466948901),
          e(113926993, 3758326383),
          e(338241895, 168717936),
          e(666307205, 1188179964),
          e(773529912, 1546045734),
          e(1294757372, 1522805485),
          e(1396182291, 2643833823),
          e(1695183700, 2343527390),
          e(1986661051, 1014477480),
          e(2177026350, 1206759142),
          e(2456956037, 344077627),
          e(2730485921, 1290863460),
          e(2820302411, 3158454273),
          e(3259730800, 3505952657),
          e(3345764771, 106217008),
          e(3516065817, 3606008344),
          e(3600352804, 1432725776),
          e(4094571909, 1467031594),
          e(275423344, 851169720),
          e(430227734, 3100823752),
          e(506948616, 1363258195),
          e(659060556, 3750685593),
          e(883997877, 3785050280),
          e(958139571, 3318307427),
          e(1322822218, 3812723403),
          e(1537002063, 2003034995),
          e(1747873779, 3602036899),
          e(1955562222, 1575990012),
          e(2024104815, 1125592928),
          e(2227730452, 2716904306),
          e(2361852424, 442776044),
          e(2428436474, 593698344),
          e(2756734187, 3733110249),
          e(3204031479, 2999351573),
          e(3329325298, 3815920427),
          e(3391569614, 3928383900),
          e(3515267271, 566280711),
          e(3940187606, 3454069534),
          e(4118630271, 4000239992),
          e(116418474, 1914138554),
          e(174292421, 2731055270),
          e(289380356, 3203993006),
          e(460393269, 320620315),
          e(685471733, 587496836),
          e(852142971, 1086792851),
          e(1017036298, 365543100),
          e(1126000580, 2618297676),
          e(1288033470, 3409855158),
          e(1501505948, 4234509866),
          e(1607167915, 987167468),
          e(1816402316, 1246189591),
        ],
        s = [],
        l = 0;
      80 > l;
      l++
    )
      s[l] = e();
    (i = i.SHA512 =
      n.extend({
        _doReset: function () {
          this._hash = new a.init([
            new r.init(1779033703, 4089235720),
            new r.init(3144134277, 2227873595),
            new r.init(1013904242, 4271175723),
            new r.init(2773480762, 1595750129),
            new r.init(1359893119, 2917565137),
            new r.init(2600822924, 725511199),
            new r.init(528734635, 4215389547),
            new r.init(1541459225, 327033209),
          ]);
        },
        _doProcessBlock: function (e, t) {
          for (
            var n = this._hash.words,
              i = n[0],
              r = n[1],
              a = n[2],
              l = n[3],
              c = n[4],
              u = n[5],
              d = n[6],
              n = n[7],
              p = i.high,
              f = i.low,
              h = r.high,
              m = r.low,
              g = a.high,
              v = a.low,
              w = l.high,
              y = l.low,
              b = c.high,
              E = c.low,
              S = u.high,
              D = u.low,
              _ = d.high,
              x = d.low,
              C = n.high,
              k = n.low,
              P = p,
              U = f,
              A = h,
              W = m,
              I = g,
              T = v,
              N = w,
              B = y,
              L = b,
              H = E,
              M = S,
              R = D,
              O = _,
              j = x,
              z = C,
              F = k,
              J = 0;
            80 > J;
            J++
          ) {
            var V = s[J];
            if (16 > J)
              var q = (V.high = 0 | e[t + 2 * J]),
                X = (V.low = 0 | e[t + 2 * J + 1]);
            else {
              var q = s[J - 15],
                X = q.high,
                $ = q.low,
                q =
                  ((X >>> 1) | ($ << 31)) ^ ((X >>> 8) | ($ << 24)) ^ (X >>> 7),
                $ =
                  (($ >>> 1) | (X << 31)) ^
                  (($ >>> 8) | (X << 24)) ^
                  (($ >>> 7) | (X << 25)),
                G = s[J - 2],
                X = G.high,
                Y = G.low,
                G =
                  ((X >>> 19) | (Y << 13)) ^
                  ((X << 3) | (Y >>> 29)) ^
                  (X >>> 6),
                Y =
                  ((Y >>> 19) | (X << 13)) ^
                  ((Y << 3) | (X >>> 29)) ^
                  ((Y >>> 6) | (X << 26)),
                X = s[J - 7],
                Q = X.high,
                K = s[J - 16],
                Z = K.high,
                K = K.low,
                X = $ + X.low,
                q = q + Q + (X >>> 0 < $ >>> 0 ? 1 : 0),
                X = X + Y,
                q = q + G + (X >>> 0 < Y >>> 0 ? 1 : 0),
                X = X + K,
                q = q + Z + (X >>> 0 < K >>> 0 ? 1 : 0);
              (V.high = q), (V.low = X);
            }
            var Q = (L & M) ^ (~L & O),
              K = (H & R) ^ (~H & j),
              V = (P & A) ^ (P & I) ^ (A & I),
              ee = (U & W) ^ (U & T) ^ (W & T),
              $ =
                ((P >>> 28) | (U << 4)) ^
                ((P << 30) | (U >>> 2)) ^
                ((P << 25) | (U >>> 7)),
              G =
                ((U >>> 28) | (P << 4)) ^
                ((U << 30) | (P >>> 2)) ^
                ((U << 25) | (P >>> 7)),
              Y = o[J],
              te = Y.high,
              ne = Y.low,
              Y =
                F +
                (((H >>> 14) | (L << 18)) ^
                  ((H >>> 18) | (L << 14)) ^
                  ((H << 23) | (L >>> 9))),
              Z =
                z +
                (((L >>> 14) | (H << 18)) ^
                  ((L >>> 18) | (H << 14)) ^
                  ((L << 23) | (H >>> 9))) +
                (Y >>> 0 < F >>> 0 ? 1 : 0),
              Y = Y + K,
              Z = Z + Q + (Y >>> 0 < K >>> 0 ? 1 : 0),
              Y = Y + ne,
              Z = Z + te + (Y >>> 0 < ne >>> 0 ? 1 : 0),
              Y = Y + X,
              Z = Z + q + (Y >>> 0 < X >>> 0 ? 1 : 0),
              X = G + ee,
              V = $ + V + (X >>> 0 < G >>> 0 ? 1 : 0),
              z = O,
              F = j,
              O = M,
              j = R,
              M = L,
              R = H,
              H = (B + Y) | 0,
              L = (N + Z + (H >>> 0 < B >>> 0 ? 1 : 0)) | 0,
              N = I,
              B = T,
              I = A,
              T = W,
              A = P,
              W = U,
              U = (Y + X) | 0,
              P = (Z + V + (U >>> 0 < Y >>> 0 ? 1 : 0)) | 0;
          }
          (f = i.low = f + U),
            (i.high = p + P + (f >>> 0 < U >>> 0 ? 1 : 0)),
            (m = r.low = m + W),
            (r.high = h + A + (m >>> 0 < W >>> 0 ? 1 : 0)),
            (v = a.low = v + T),
            (a.high = g + I + (v >>> 0 < T >>> 0 ? 1 : 0)),
            (y = l.low = y + B),
            (l.high = w + N + (y >>> 0 < B >>> 0 ? 1 : 0)),
            (E = c.low = E + H),
            (c.high = b + L + (E >>> 0 < H >>> 0 ? 1 : 0)),
            (D = u.low = D + R),
            (u.high = S + M + (D >>> 0 < R >>> 0 ? 1 : 0)),
            (x = d.low = x + j),
            (d.high = _ + O + (x >>> 0 < j >>> 0 ? 1 : 0)),
            (k = n.low = k + F),
            (n.high = C + z + (k >>> 0 < F >>> 0 ? 1 : 0));
        },
        _doFinalize: function () {
          var e = this._data,
            t = e.words,
            n = 8 * this._nDataBytes,
            i = 8 * e.sigBytes;
          return (
            (t[i >>> 5] |= 128 << (24 - (i % 32))),
            (t[30 + (((i + 128) >>> 10) << 5)] = Math.floor(n / 4294967296)),
            (t[31 + (((i + 128) >>> 10) << 5)] = n),
            (e.sigBytes = 4 * t.length),
            this._process(),
            this._hash.toX32()
          );
        },
        clone: function () {
          var e = n.clone.call(this);
          return (e._hash = this._hash.clone()), e;
        },
        blockSize: 32,
      })),
      (t.SHA512 = n._createHelper(i)),
      (t.HmacSHA512 = n._createHmacHelper(i));
  })(),
  define("libs/cryptojs/sha512", function () {});
var CryptoJS =
  CryptoJS ||
  (function (e, t) {
    var n = {},
      i = (n.lib = {}),
      r = function () {},
      a = (i.Base = {
        extend: function (e) {
          r.prototype = this;
          var t = new r();
          return (
            e && t.mixIn(e),
            t.hasOwnProperty("init") ||
              (t.init = function () {
                t.$super.init.apply(this, arguments);
              }),
            (t.init.prototype = t),
            (t.$super = this),
            t
          );
        },
        create: function () {
          var e = this.extend();
          return e.init.apply(e, arguments), e;
        },
        init: function () {},
        mixIn: function (e) {
          for (var t in e) e.hasOwnProperty(t) && (this[t] = e[t]);
          e.hasOwnProperty("toString") && (this.toString = e.toString);
        },
        clone: function () {
          return this.init.prototype.extend(this);
        },
      }),
      o = (i.WordArray = a.extend({
        init: function (e, t) {
          (e = this.words = e || []),
            (this.sigBytes = void 0 != t ? t : 4 * e.length);
        },
        toString: function (e) {
          return (e || l).stringify(this);
        },
        concat: function (e) {
          var t = this.words,
            n = e.words,
            i = this.sigBytes;
          if (((e = e.sigBytes), this.clamp(), i % 4))
            for (var r = 0; r < e; r++)
              t[(i + r) >>> 2] |=
                ((n[r >>> 2] >>> (24 - (r % 4) * 8)) & 255) <<
                (24 - ((i + r) % 4) * 8);
          else if (65535 < n.length)
            for (r = 0; r < e; r += 4) t[(i + r) >>> 2] = n[r >>> 2];
          else t.push.apply(t, n);
          return (this.sigBytes += e), this;
        },
        clamp: function () {
          var t = this.words,
            n = this.sigBytes;
          (t[n >>> 2] &= 4294967295 << (32 - (n % 4) * 8)),
            (t.length = e.ceil(n / 4));
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e.words = this.words.slice(0)), e;
        },
        random: function (t) {
          for (var n = [], i = 0; i < t; i += 4)
            n.push((4294967296 * e.random()) | 0);
          return new o.init(n, t);
        },
      })),
      s = (n.enc = {}),
      l = (s.Hex = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++) {
            var r = (t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255;
            n.push((r >>> 4).toString(16)), n.push((15 & r).toString(16));
          }
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i += 2)
            n[i >>> 3] |= parseInt(e.substr(i, 2), 16) << (24 - (i % 8) * 4);
          return new o.init(n, t / 2);
        },
      }),
      c = (s.Latin1 = {
        stringify: function (e) {
          var t = e.words;
          e = e.sigBytes;
          for (var n = [], i = 0; i < e; i++)
            n.push(
              String.fromCharCode((t[i >>> 2] >>> (24 - (i % 4) * 8)) & 255)
            );
          return n.join("");
        },
        parse: function (e) {
          for (var t = e.length, n = [], i = 0; i < t; i++)
            n[i >>> 2] |= (255 & e.charCodeAt(i)) << (24 - (i % 4) * 8);
          return new o.init(n, t);
        },
      }),
      u = (s.Utf8 = {
        stringify: function (e) {
          try {
            return decodeURIComponent(escape(c.stringify(e)));
          } catch (e) {
            throw Error("Malformed UTF-8 data");
          }
        },
        parse: function (e) {
          return c.parse(unescape(encodeURIComponent(e)));
        },
      }),
      d = (i.BufferedBlockAlgorithm = a.extend({
        reset: function () {
          (this._data = new o.init()), (this._nDataBytes = 0);
        },
        _append: function (e) {
          "string" == typeof e && (e = u.parse(e)),
            this._data.concat(e),
            (this._nDataBytes += e.sigBytes);
        },
        _process: function (t) {
          var n = this._data,
            i = n.words,
            r = n.sigBytes,
            a = this.blockSize,
            s = r / (4 * a),
            s = t ? e.ceil(s) : e.max((0 | s) - this._minBufferSize, 0);
          if (((t = s * a), (r = e.min(4 * t, r)), t)) {
            for (var l = 0; l < t; l += a) this._doProcessBlock(i, l);
            (l = i.splice(0, t)), (n.sigBytes -= r);
          }
          return new o.init(l, r);
        },
        clone: function () {
          var e = a.clone.call(this);
          return (e._data = this._data.clone()), e;
        },
        _minBufferSize: 0,
      }));
    i.Hasher = d.extend({
      cfg: a.extend(),
      init: function (e) {
        (this.cfg = this.cfg.extend(e)), this.reset();
      },
      reset: function () {
        d.reset.call(this), this._doReset();
      },
      update: function (e) {
        return this._append(e), this._process(), this;
      },
      finalize: function (e) {
        return e && this._append(e), this._doFinalize();
      },
      blockSize: 16,
      _createHelper: function (e) {
        return function (t, n) {
          return new e.init(n).finalize(t);
        };
      },
      _createHmacHelper: function (e) {
        return function (t, n) {
          return new p.HMAC.init(e, n).finalize(t);
        };
      },
    });
    var p = (n.algo = {});
    return n;
  })(Math);
(function (e) {
  var t = CryptoJS,
    n = t.lib,
    i = n.Base,
    r = n.WordArray,
    t = (t.x64 = {});
  (t.Word = i.extend({
    init: function (e, t) {
      (this.high = e), (this.low = t);
    },
  })),
    (t.WordArray = i.extend({
      init: function (e, t) {
        (e = this.words = e || []),
          (this.sigBytes = void 0 != t ? t : 8 * e.length);
      },
      toX32: function () {
        for (var e = this.words, t = e.length, n = [], i = 0; i < t; i++) {
          var a = e[i];
          n.push(a.high), n.push(a.low);
        }
        return r.create(n, this.sigBytes);
      },
      clone: function () {
        for (
          var e = i.clone.call(this),
            t = (e.words = this.words.slice(0)),
            n = t.length,
            r = 0;
          r < n;
          r++
        )
          t[r] = t[r].clone();
        return e;
      },
    }));
})(),
  (function (e) {
    for (
      var t = CryptoJS,
        n = t.lib,
        i = n.WordArray,
        r = n.Hasher,
        a = t.x64.Word,
        n = t.algo,
        o = [],
        s = [],
        l = [],
        c = 1,
        u = 0,
        d = 0;
      24 > d;
      d++
    ) {
      o[c + 5 * u] = (((d + 1) * (d + 2)) / 2) % 64;
      var p = (2 * c + 3 * u) % 5,
        c = u % 5,
        u = p;
    }
    for (c = 0; 5 > c; c++)
      for (u = 0; 5 > u; u++) s[c + 5 * u] = u + ((2 * c + 3 * u) % 5) * 5;
    for (c = 1, u = 0; 24 > u; u++) {
      for (var f = (p = d = 0); 7 > f; f++) {
        if (1 & c) {
          var h = (1 << f) - 1;
          32 > h ? (p ^= 1 << h) : (d ^= 1 << (h - 32));
        }
        c = 128 & c ? (c << 1) ^ 113 : c << 1;
      }
      l[u] = a.create(d, p);
    }
    for (var m = [], c = 0; 25 > c; c++) m[c] = a.create();
    (n = n.SHA3 =
      r.extend({
        cfg: r.cfg.extend({ outputLength: 512 }),
        _doReset: function () {
          for (var e = (this._state = []), t = 0; 25 > t; t++)
            e[t] = new a.init();
          this.blockSize = (1600 - 2 * this.cfg.outputLength) / 32;
        },
        _doProcessBlock: function (e, t) {
          for (var n = this._state, i = this.blockSize / 2, r = 0; r < i; r++) {
            var a = e[t + 2 * r],
              c = e[t + 2 * r + 1],
              a =
                (16711935 & ((a << 8) | (a >>> 24))) |
                (4278255360 & ((a << 24) | (a >>> 8))),
              c =
                (16711935 & ((c << 8) | (c >>> 24))) |
                (4278255360 & ((c << 24) | (c >>> 8))),
              u = n[r];
            (u.high ^= c), (u.low ^= a);
          }
          for (i = 0; 24 > i; i++) {
            for (r = 0; 5 > r; r++) {
              for (var d = (a = 0), p = 0; 5 > p; p++)
                (u = n[r + 5 * p]), (a ^= u.high), (d ^= u.low);
              (u = m[r]), (u.high = a), (u.low = d);
            }
            for (r = 0; 5 > r; r++)
              for (
                u = m[(r + 4) % 5],
                  a = m[(r + 1) % 5],
                  c = a.high,
                  p = a.low,
                  a = u.high ^ ((c << 1) | (p >>> 31)),
                  d = u.low ^ ((p << 1) | (c >>> 31)),
                  p = 0;
                5 > p;
                p++
              )
                (u = n[r + 5 * p]), (u.high ^= a), (u.low ^= d);
            for (c = 1; 25 > c; c++)
              (u = n[c]),
                (r = u.high),
                (u = u.low),
                (p = o[c]),
                32 > p
                  ? ((a = (r << p) | (u >>> (32 - p))),
                    (d = (u << p) | (r >>> (32 - p))))
                  : ((a = (u << (p - 32)) | (r >>> (64 - p))),
                    (d = (r << (p - 32)) | (u >>> (64 - p)))),
                (u = m[s[c]]),
                (u.high = a),
                (u.low = d);
            for (
              u = m[0], r = n[0], u.high = r.high, u.low = r.low, r = 0;
              5 > r;
              r++
            )
              for (p = 0; 5 > p; p++)
                (c = r + 5 * p),
                  (u = n[c]),
                  (a = m[c]),
                  (c = m[((r + 1) % 5) + 5 * p]),
                  (d = m[((r + 2) % 5) + 5 * p]),
                  (u.high = a.high ^ (~c.high & d.high)),
                  (u.low = a.low ^ (~c.low & d.low));
            (u = n[0]), (r = l[i]), (u.high ^= r.high), (u.low ^= r.low);
          }
        },
        _doFinalize: function () {
          var t = this._data,
            n = t.words,
            r = 8 * t.sigBytes,
            a = 32 * this.blockSize;
          (n[r >>> 5] |= 1 << (24 - (r % 32))),
            (n[((e.ceil((r + 1) / a) * a) >>> 5) - 1] |= 128),
            (t.sigBytes = 4 * n.length),
            this._process();
          for (
            var t = this._state,
              n = this.cfg.outputLength / 8,
              r = n / 8,
              a = [],
              o = 0;
            o < r;
            o++
          ) {
            var s = t[o],
              l = s.high,
              s = s.low,
              l =
                (16711935 & ((l << 8) | (l >>> 24))) |
                (4278255360 & ((l << 24) | (l >>> 8))),
              s =
                (16711935 & ((s << 8) | (s >>> 24))) |
                (4278255360 & ((s << 24) | (s >>> 8)));
            a.push(s), a.push(l);
          }
          return new i.init(a, n);
        },
        clone: function () {
          for (
            var e = r.clone.call(this),
              t = (e._state = this._state.slice(0)),
              n = 0;
            25 > n;
            n++
          )
            t[n] = t[n].clone();
          return e;
        },
      })),
      (t.SHA3 = r._createHelper(n)),
      (t.HmacSHA3 = r._createHmacHelper(n));
  })(Math),
  define("libs/cryptojs/sha3", function () {}),
  define(
    "DS/W3DPassport/dsp/utils/fingerprint",
    [
      "libs/cryptojs/md5",
      "libs/cryptojs/sha256",
      "libs/cryptojs/sha512",
      "libs/cryptojs/sha3",
    ],
    function (e, t, n, i) {
      "use strict";
      var r = {
          md5: CryptoJS.MD5,
          sha256: CryptoJS.SHA256,
          sha512: CryptoJS.SHA512,
          sha3: CryptoJS.SHA3,
        },
        a = function () {
          return "#" + screen.height + "x" + screen.width + "#";
        },
        o = function () {
          return (
            "#" +
            navigator.userAgent +
            "@" +
            navigator.cpuClass +
            "@" +
            navigator.platform +
            "#"
          );
        },
        s = function () {
          var e,
            t = (function (e) {
              if (window.WebGLRenderingContext) {
                for (
                  var t = document.createElement("canvas"),
                    n = [
                      "webgl",
                      "experimental-webgl",
                      "moz-webgl",
                      "webkit-3d",
                    ],
                    i = !1,
                    r = 0;
                  r < 4;
                  r++
                )
                  try {
                    if (
                      (i = t.getContext(n[r])) &&
                      "function" == typeof i.getParameter
                    )
                      return !e || { name: n[r], gl: i };
                  } catch (e) {}
                return !1;
              }
              return !1;
            })(1);
          if (t) {
            e += "#";
            var n = t.gl;
            (e += "contextName:" + t.name),
              (e += "-version:" + n.getParameter(n.VERSION)),
              (e +=
                "-shadinglanguageversion:" +
                n.getParameter(n.SHADING_LANGUAGE_VERSION)),
              (e += "-renderer:" + n.getParameter(n.RENDERER));
            var i = [];
            try {
              i = n.getSupportedExtensions();
            } catch (l) {}
            var r = i.length;
            if (r) {
              for (var a = "", o = 0; o < r; o++)
                a.length && (a += "; "), (a += i[o]);
              e += "-exts:" + a;
            }
            var s,
              l =
                n.getExtension("EXT_texture_filter_anisotropic") ||
                n.getExtension("WEBKIT_EXT_texture_filter_anisotropic") ||
                n.getExtension("MOZ_EXT_texture_filter_anisotropic");
            l
              ? 0 === (s = n.getParameter(l.MAX_TEXTURE_MAX_ANISOTROPY_EXT)) &&
                (s = 2)
              : (s = "Not available"),
              (e += "-maxan:" + s),
              (e += "#");
          } else e = "#nowebgl#";
          return e;
        };
      return function (e) {
        var t = a(),
          n = e.seed || 1;
        (t += o()), e.webgl && (t += s()), (t += "##"), (t += "##");
        var i = null;
        e.hasher && (i = r[e.hasher]), i || (i = CryptoJS.SHA3);
        try {
          localStorage.fingerprint = t;
        } catch (e) {
          console.log("localStorage unavailable");
        }
        return i(t + n).toString();
      };
    }
  ),
  define(
    "DS/W3DPassport/dsp/utils/addValidation",
    ["DS/W3DPassport/dsp/UWA", "DS/W3DPassport/dsp/utils/contains"],
    function (e, t) {
      "use strict";
      function n(e) {
        var n,
          i = !0;
        return (
          "FIELDSET" === e.nodeName
            ? (i = !1)
            : "BUTTON" === e.nodeName
            ? (i = !1)
            : "INPUT" === e.nodeName &&
              ((n = e.type || e.getAttribute("type")), t(f, n) && (i = !1)),
          i
        );
      }
      function i(e) {
        return e.checkValidity();
      }
      function r(e) {
        var t = !0;
        return (
          e.hasAttribute(p.REQUIRED) &&
            !e.hasAttribute(p.DISABLED) &&
            (t = 0 < e.value.length),
          t
        );
      }
      function a(e) {
        var t,
          n = !0;
        return (
          e.hasAttribute(p.MIN_LENGTH) &&
            ((t = parseInt(e.getAttribute(p.MIN_LENGTH), 10)),
            isNaN(t) || (n = t < e.value.length)),
          n
        );
      }
      function o(e) {
        var t,
          n = !0;
        return (
          e.hasAttribute(p.MAX_LENGTH) &&
            ((t = parseInt(e.getAttribute(p.MAX_LENGTH), 10)),
            isNaN(t) || (n = t > e.value.length)),
          n
        );
      }
      function s(e) {
        var t,
          n = !0;
        return (
          e.hasAttribute(p.PATTERN) &&
            ((t = new RegExp("^(" + e.getAttribute(p.PATTERN) + ")$")),
            (n = t.test(e.value))),
          n
        );
      }
      function l(e) {
        var t,
          n = e.getAttribute("name"),
          i = !0;
        return (
          n &&
            n.test("_confirm") &&
            (t = document.querySelector(
              "[name=" +
                n
                  .replace("_confirm", "")
                  .replace("[", "\\[")
                  .replace("]", "\\]")
                  .replace(".", "\\.") +
                "]"
            )) &&
            (i = e.value === t.value),
          i
        );
      }
      function c(t) {
        var n,
          c,
          u = [],
          d = !0;
        for (
          "function" == typeof t.checkValidity
            ? u.push(i)
            : (u.push(r), u.push(o), u.push(s)),
            u.push(l),
            u.push(a),
            n = u.length,
            c = 0;
          c < n && d;
          c += 1
        )
          d = d && u[c](t);
        return (
          d
            ? (e.Element.removeClassName.call(t, "invalid"),
              e.Element.addClassName.call(t, "valid"))
            : (e.Element.removeClassName.call(t, "valid"),
              e.Element.addClassName.call(t, "invalid")),
          d
        );
      }
      function u(t) {
        var i,
          r,
          a,
          o,
          s = e.Event.findElement(t, "form"),
          l = !1;
        if (s)
          for (i = s.elements, a = i.length, o = 0; o < a; o += 1)
            (r = i[o]),
              n(r) && (c(r) || l || (e.Event.stop(t), r.focus(), (l = !0)));
      }
      function d(t) {
        var i = e.Event.findElement(t, ".uwa-input");
        i && n(i) && c(i);
      }
      var p = {
          DISABLED: "disabled",
          REQUIRED: "required",
          MIN_LENGTH: "minLength",
          MAX_LENGTH: "maxLength",
          PATTERN: "pattern",
        },
        f = ["submit", "cancel", "button"];
      return function (t) {
        "FORM" === t.nodeName &&
          (t.setAttribute("novalidate", "novalidate"),
          e.Element.addEvent.call(t, "submit", u),
          Modernizr.hasEvent("input", t)
            ? e.Element.addEvent.call(t, "input", d)
            : e.Element.addEvent.call(t, "focusout", d));
      };
    }
  ),
  define(
    "DS/W3DPassport/dsp/form/createLoginForm",
    [
      "DS/W3DPassport/dsp/UWA",
      "DS/W3DPassport/dsp/utils/areRequiredOptionsMissing",
      "DS/W3DPassport/dsp/utils/addCaptchaToFields",
      "DS/W3DPassport/dsp/utils/fingerprint",
      "DS/W3DPassport/dsp/utils/addValidation",
    ],
    function (e, t, n, i, r) {
      function a(e) {
        return "true" === e;
      }
      return function (o) {
        var s, l, c, u, d, p;
        if (
          ((s = { displayCaptcha: !1 }),
          e.extend(s, o),
          (s.isMultiSite = "@@is_multi_site@@"),
          (d = s.username || ""),
          (p = ""),
          a(s.isMultiSite))
        )
          try {
            if (
              !(p = localStorage.getItem("x3ds_siteid") || "") &&
              location.search
            ) {
              var f = e.Utils.parseQuery(location.search);
              f.x3ds_siteid
                ? (p = f.x3ds_siteid)
                : f.service
                ? ((f = e.Utils.parseQuery(e.Utils.parseUrl(f.service).query)),
                  f.x3ds_siteid && (p = f.x3ds_siteid))
                : f.redirect &&
                  ((f = e.Utils.parseQuery(
                    e.Utils.parseUrl(atob(f.redirect)).query
                  )),
                  f.x3ds_siteid && (p = f.x3ds_siteid));
            }
          } catch (e) {}
        if (t(s, ["url", "loginTicket"]))
          throw new Error("createLoginForm() required options are missing.");
        if (
          ((u = [
            {
              className: "hidden",
              type: "hidden",
              value: s.loginTicket,
              attributes: { name: "lt" },
            },
            {
              className: "hidden",
              type: "hidden",
              value: i({ webgl: !0, seed: s.seed }),
              attributes: { name: "fp" },
            },
            {
              className: "username",
              type: "text",
              label: "User",
              value: d,
              attributes: {
                name: "username",
                "data-dsp-i18n": "field.username_or_email.label",
                tabindex: 1,
                autofocus: "autofocus",
                placeholder: a(s.isMultiSite)
                  ? "Username"
                  : "Email or username",
                required: "required",
              },
            },
            {
              className: "password",
              type: "password",
              label: "Password",
              attributes: {
                name: "password",
                "data-dsp-i18n": "field.password.label",
                tabindex: 2,
                placeholder: "Password",
                required: "required",
              },
            },
          ]),
          a(s.isMultiSite))
        ) {
          var h = {
            type: "text",
            label: "Site ID",
            value: p,
            attributes: { name: "siteid", tabindex: 3, placeholder: "Site ID" },
          };
          location.pathname.contains("/admin-tools/login") ||
            (h.attributes.required = "required"),
            u.push(h);
        }
        if (
          (s.displayCaptcha && ((s.needsCaptcha = !0), n(s, u)),
          u.push({
            className: "remember-me",
            type: "checkbox",
            label: "Remember me",
            attributes: {
              name: "rememberMe",
              "data-dsp-i18n": "field.rememberme.label",
              tabindex: 4,
            },
          }),
          (c = new e.dsp.component.InputForm({
            className: "uwa-input-form login-form",
            legend: {
              value: "3DEXPERIENCE ID",
              attributes: {
                "data-dsp-i18n":
                  void 0 !== s.footnoteNls &&
                  "" !== s.footnoteNls &&
                  null !== s.footnoteNls
                    ? "commons.panel.logIn.title.with.footnote"
                    : "commons.panel.logIn.title",
                class: "login-legend",
              },
            },
            fields: u,
            commands: [
              {
                type: "submit",
                value: "Log in",
                attributes: {
                  "data-dsp-i18n": "commons.action.logIn",
                  tabindex: 5,
                },
              },
            ],
          })),
          s.authorizeRememberMe ||
            c.elements.container
              .getElement(".field.remember-me>label")
              .remove(),
          (l = c.getContent()),
          l.removeEvent("submit"),
          (l.action = s.url),
          (l.method = "POST"),
          r(l),
          a(s.isMultiSite))
        ) {
          var u = l.elements,
            m = u.username,
            g = u.siteid;
          m.addEventListener(
            "blur",
            function (e) {
              var t = m.value,
                n = t.lastIndexOf("@"),
                i = t.indexOf(".", n);
              n > 0 &&
                i < 0 &&
                ((m.value = t.substring(0, n)),
                g && (g.value = t.substring(n + 1)));
            },
            !0
          ),
            e.Element.addEvent.call(l, "submit", function (e) {
              if (m && g) {
                var t = m.value,
                  n = g.value;
                if (t && n) {
                  var i = t.lastIndexOf("@"),
                    r = t.indexOf(".", i);
                  m.value = i > 0 && r < 0 ? t : t + "@" + n;
                }
              }
            });
        }
        return c;
      };
    }
  ),
  define("DS/W3DPassport/dsp/utils/decodeHtml", [], function () {
    "use strict";
    return function (e) {
      var t = document.createElement("textarea");
      return (t.innerHTML = e), t.value;
    };
  }),
  define(
    "DS/W3DPassport/dsp/form/createUserForm",
    [
      "DS/W3DPassport/dsp/UWA",
      "DS/W3DPassport/dsp/utils/areRequiredOptionsMissing",
      "DS/W3DPassport/dsp/utils/addCaptchaToFields",
      "DS/W3DPassport/dsp/utils/addCsrfToFields",
      "DS/W3DPassport/dsp/utils/addValidation",
      "DS/W3DPassport/dsp/utils/decodeHtml",
    ],
    function (e, t, n, i, r, a) {
      "use strict";
      return function (o) {
        var s, l, c, u, d;
        if (
          ((s = { values: {}, showPasswordFields: !1, ajax: !1 }),
          e.extend(s, o),
          t(s, ["url", "apiUrlCountries"]))
        )
          throw new Error("createUserForm() required options are missing");
        l = s.fields;
        for (var p = 0; p < l.length; p++)
          l[p].value && (l[p].value = a(l[p].value));
        return (
          i(s, l),
          "commons.action.register" == s.actionI18n &&
            void 0 !== s.captchaRegister &&
            "true" === s.captchaRegister &&
            ((s.needsCaptcha = !0), n(s, l)),
          (c =
            1 == s.updateAllowed || 1 == s.createAllowed
              ? [
                  {
                    type: "submit",
                    value: "Submit",
                    attributes: { "data-dsp-i18n": s.actionI18n },
                  },
                ]
              : []),
          s.cancelUrl &&
            c.push({
              type: "cancel",
              value: "Cancel",
              attributes: { "data-dsp-i18n": "commons.action.cancel" },
              events: {
                onClick: function (e) {
                  window.location.href = s.cancelUrl;
                },
              },
            }),
          (u = new e.dsp.component.InputForm({
            fields: l,
            className: s.className,
            commands: c,
            events: {
              onSubmit: function (t, n) {
                e.Data.request(s.url, { method: "POST", data: n });
              },
            },
          })),
          (d = u.getContent()),
          s.ajax ||
            (d.removeEvent("submit"), (d.action = s.url), (d.method = "POST")),
          r(d),
          u
        );
      };
    }
  ),
  define(
    "DS/W3DPassport/dsp/form/createRegisterForm",
    ["DS/W3DPassport/dsp/UWA", "DS/W3DPassport/dsp/form/createUserForm"],
    function (e, t) {
      "use strict";
      return function (n) {
        var i;
        return (
          (i = {
            className: "uwa-input-form register-form",
            actionI18n: "commons.action.register",
            showPasswordFields: !0,
            ajax: !1,
          }),
          e.extend(i, n),
          t(i)
        );
      };
    }
  ),
  define(
    "DS/W3DPassport/dsp/utils/loginTicketTimeoutMsg",
    ["DS/W3DPassport/dsp/UWA"],
    function (e) {
      "use strict";
      function t() {
        var t = e.Element.getElement.call(document.body, "[name=lt]");
        e.Data.getJson(o, function (e) {
          t.value = e.lt;
        });
      }
      function n() {
        document[i]
          ? window.clearInterval(a)
          : (t(), (a = window.setInterval(t, s)));
      }
      var i,
        r,
        a,
        o,
        s = 119e3;
      return function (l) {
        (o = l),
          void 0 !== document.hidden
            ? ((i = "hidden"), (r = "visibilitychange"))
            : void 0 !== document.mozHidden
            ? ((i = "mozHidden"), (r = "mozvisibilitychange"))
            : void 0 !== document.msHidden
            ? ((i = "msHidden"), (r = "msvisibilitychange"))
            : void 0 !== document.webkitHidden &&
              ((i = "webkitHidden"), (r = "webkitvisibilitychange")),
          "function" == typeof document.addEventListener && i
            ? ((a = window.setInterval(t, s)),
              document.addEventListener(r, n, !1))
            : window.setTimeout(function () {
                var t,
                  n = e.Element.getElement.call(document, ".login-form"),
                  i = n.getElement(".commands"),
                  r = e.Element.getElement.call(document, ".lt-expired-error");
                for (t = n.elements.length; 0 < t; )
                  (t -= 1), (n.elements[t].disabled = !0);
                i.hide(),
                  r.removeClassName("hidden"),
                  window.setTimeout(function () {
                    r.addClassName("active");
                  }, 0);
              }, 12e4);
      };
    }
  ),
  define("DS/W3DPassport/dsp/utils/regExpUtil", [], function () {
    "use strict";
    var e = {};
    return (
      (e.escapeSpecialChars = function (e) {
        return e ? e.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&") : e;
      }),
      e
    );
  }),
  define(
    "DS/W3DPassport/dsp/utils/createPasswordInformationPanel",
    ["DS/W3DPassport/dsp/i18n/i18n", "DS/W3DPassport/dsp/utils/regExpUtil"],
    function (e, t) {
      function n(e) {
        return UWA.Element.getElements.call(
          document,
          "[name=data\\[" + e + "\\]]"
        )[0];
      }
      function i(e) {
        return c && l !== e
          ? c.fields[e]
          : UWA.Element.getElements.call(
              document,
              "[name=data\\[" + e + "\\]]"
            )[0].value || "";
      }
      function r(e, t) {
        var n = UWA.Element.getElements.call(
          document,
          ".rule[name=" + e.replace(/\./g, "\\.") + "]"
        )[0];
        n &&
          (t
            ? (n.removeClassName("ko"), n.addClassName("ok"))
            : (n.addClassName("ko"), n.removeClassName("ok")));
      }
      function a(e, n) {
        return e.match(new RegExp(t.escapeSpecialChars(n), "gi"));
      }
      function o(t) {
        var n = UWA.Element.getElements.call(
            document,
            "[name=data\\[" + l + "\\]]"
          )[0],
          o = n.value,
          s = o ? o.length : 0,
          c = "bad",
          u = !0,
          d = t.specialCharactersAllowed.length,
          p = n.getParent(".field").getElement(".password-tooltip");
        if (!t.allowPasswordWithUsername) {
          var f = i("username");
          o && f && "" != f && a(o, f)
            ? ((u = !1), r("rule.pwd.allow.username", !1))
            : r("rule.pwd.allow.username", !0);
        }
        if (!t.allowPasswordWithFirstname) {
          var h = i("firstName");
          o && h && "" != h && a(o, h)
            ? ((u = !1), r("rule.pwd.allow.firstName", !1))
            : r("rule.pwd.allow.firstName", !0);
        }
        if (!t.allowPasswordWithLastname) {
          var m = i("lastName");
          o && m && "" != m && a(o, m)
            ? ((u = !1), r("rule.pwd.allow.lastName", !1))
            : r("rule.pwd.allow.lastName", !0);
        }
        t.minimumLength > 0 &&
          (o.length >= t.minimumLength
            ? r("rule.pwd.minLength", !0)
            : (r("rule.pwd.minLength", !1), (u = !1)));
        var g,
          v = o.replace(/[^0-9]/g, "").length,
          w = o.replace(/[^a-z]/g, "").length,
          y = o.replace(/[^A-Z]/g, "").length,
          b = 0,
          E = 0;
        for (g = 0; g < s; g++) {
          var S = o[g];
          ("0" <= S && S <= "9") ||
            ("a" <= S && S <= "z") ||
            ("A" <= S && S <= "Z") ||
            (t.specialCharactersAllowed.indexOf(S) >= 0 ? b++ : E++);
        }
        if (
          (t.minimumDigits > 0 &&
            (v >= t.minimumDigits
              ? r("rule.pwd.minDigits", !0)
              : (r("rule.pwd.minDigits", !1), (u = !1))),
          t.minimumLetters > 0 &&
            (w + y >= t.minimumLetters
              ? r("rule.pwd.minLetters", !0)
              : (r("rule.pwd.minLetters", !1), (u = !1))),
          t.minimumLowerCase > 0 &&
            (w >= t.minimumLowerCase
              ? r("rule.pwd.minLowerCase", !0)
              : (r("rule.pwd.minLowerCase", !1), (u = !1))),
          t.minimumUpperCase > 0 &&
            (y >= t.minimumUpperCase
              ? r("rule.pwd.minUpperCase", !0)
              : (r("rule.pwd.minUpperCase", !1), (u = !1))),
          t.minimumSpecial > 0 &&
            (b >= t.minimumSpecial
              ? r("rule.pwd.minSpecial", !0)
              : (r("rule.pwd.minSpecial", !1), (u = !1))),
          t.specialCharactersAllowed.length > 0 &&
            (E > 0
              ? (r("rule.pwd.special.allowed", !1), (u = !1))
              : r("rule.pwd.special.allowed", !0)),
          u)
        ) {
          var D = (o.length * Math.log(62 + d)) / Math.LN2;
          (c = D < 49 ? "low" : D < 100 ? "medium" : "high"),
            UWA.Element.removeClassName.call(n, "invalid"),
            UWA.Element.addClassName.call(n, "valid");
        } else
          (c = "bad"),
            UWA.Element.removeClassName.call(n, "valid"),
            UWA.Element.addClassName.call(n, "invalid");
        return (
          p.setData("data-dsp-i18n", "password.level." + c),
          p.setText(e.translate("password.level." + c)),
          UWA.Element.getElements
            .call(document, ".passwordInfoStrength div")
            .forEach(function (e) {
              e.removeClassName("selected");
            }),
          UWA.Element.getElements
            .call(document, ".passwordInfoStrength ." + c)
            .forEach(function (e) {
              e.addClassName("selected");
            }),
          u
        );
      }
      function s(e, t) {
        var n,
          i = UWA.Element.getElement.call(
            document,
            "[name=data\\[" + e + "\\]]"
          );
        (n = UWA.createElement("div", {
          class: "password-tooltip fonticon fonticon-info site-icon",
          "data-dsp-i18n": "password.level.bad",
          events: {
            click: function (e) {
              t.toggleClassName("invisible");
            },
          },
        })),
          i.parentNode.insertBefore(n, i.parentNode.firstChild.nextSibling);
      }
      var l, c;
      return function (e, t, i) {
        (l = e), (c = i.user);
        var r = UWA.Element.getElements.call(
            document,
            "[name=data\\[" + l + "\\]]"
          ),
          a = i.passwordPolicy;
        r &&
          r.length > 0 &&
          r.forEach(function (i) {
            var r, l;
            (r = new UWA.Element("div", {
              class: "passwordInfoStrength",
              html: [
                new UWA.Element("div", { class: "bad" }),
                new UWA.Element("div", { class: "low" }),
                new UWA.Element("div", { class: "medium" }),
                new UWA.Element("div", { class: "high" }),
              ],
            })),
              (l = new UWA.Element("div", {
                class: "passwordInfo",
                html: [
                  {
                    tag: "div",
                    class: "passwordInfoRules",
                    html: (function () {
                      var e = [],
                        t = function (e, t) {
                          var n = {
                            class: "description",
                            "data-dsp-i18n": e,
                            text: e,
                          };
                          return (
                            t && t.length > 0 && (n["data-dsp-i18n-param"] = t),
                            new UWA.Element("div", {
                              class: "rule",
                              name: e,
                              html: UWA.Element("div", n),
                            })
                          );
                        };
                      return (
                        a.allowPasswordWithUsername ||
                          e.push(t("rule.pwd.allow.username")),
                        a.allowPasswordWithFirstname ||
                          e.push(t("rule.pwd.allow.firstName")),
                        a.allowPasswordWithLastname ||
                          e.push(t("rule.pwd.allow.lastName")),
                        a.minimumLength > 0 &&
                          e.push(t("rule.pwd.minLength", [a.minimumLength])),
                        a.minimumDigits > 0 &&
                          e.push(t("rule.pwd.minDigits", [a.minimumDigits])),
                        a.minimumLetters > 0 &&
                          e.push(t("rule.pwd.minLetters", [a.minimumLetters])),
                        a.minimumLowerCase > 0 &&
                          e.push(
                            t("rule.pwd.minLowerCase", [a.minimumLowerCase])
                          ),
                        a.minimumUpperCase > 0 &&
                          e.push(
                            t("rule.pwd.minUpperCase", [a.minimumUpperCase])
                          ),
                        a.minimumSpecial > 0 &&
                          e.push(t("rule.pwd.minSpecial", [a.minimumSpecial])),
                        a.specialCharactersAllowed &&
                          a.specialCharactersAllowed.length > 0 &&
                          e.push(
                            t("rule.pwd.special.allowed", [
                              a.specialCharactersAllowed
                                .map(function (e) {
                                  return "," === e ? "#######COMMA#######" : e;
                                })
                                .join(" "),
                            ])
                          ),
                        e
                      );
                    })(),
                  },
                ],
              })),
              r.inject(i.getParent(".field"), "bottom"),
              l.inject(i.getParent(".field"), "bottom"),
              l.addClassName("invisible"),
              UWA.Element.addEvent.call(i, "focus", function (e) {
                o(a);
              }),
              UWA.Element.addEvent.call(i, "focusout", function (e) {
                l.addClassName("invisible");
              }),
              UWA.Element.addEvent.call(i, "keyup", function (e) {
                o(a);
              }),
              c ||
                ["username", "firstName", "lastName"].forEach(function (e) {
                  var t = n(e);
                  UWA.Element.addEvent.call(t, "keyup", function (e) {
                    o(a);
                  });
                }),
              UWA.Element.addEvent.call(t, "submit", function (e) {
                o(a) || (UWA.Event.stop(e), i.focus());
              }),
              s(e, l);
          });
      };
    }
  ),
  define("DS/W3DPassport/dsp/utils/hightlightBackendError", [], function () {
    "use strict";
    function e(e, t) {
      var n, i;
      if (Array.isArray(t) && void 0 !== e && "function" == typeof e.getElement)
        for (i = t.length; i > 0; )
          (i -= 1),
            (n = e.getElement('[name="data[' + t[i] + ']"]')) &&
              n.addClassName("invalid");
    }
    return function (t, n) {
      var i,
        r = n.errorMsgs;
      if (void 0 !== r && void 0 !== r.length && r.length > 0)
        for (i = r.length; i > 0; ) (i -= 1), e(t, r[i].affectedFieldNames);
    };
  }),
  define("DS/WebappsUtils/Map", [], function () {
    function e(e, t) {
      for (var n = 0; n < e.size; ++n) if (e._content[n].key === t) return n;
      return -1;
    }
    var t = function () {
      Object.defineProperty(this, "_content", { value: [] }),
        Object.defineProperty(this, "size", {
          get: function () {
            return this._content.length;
          },
        });
    };
    return (
      (t.prototype.get = function (e) {
        return (
          this._content.filter(function (t) {
            return t.key === e;
          })[0] || {}
        ).value;
      }),
      (t.prototype.set = function (t, n) {
        var i = e(this, t);
        return (
          i > -1
            ? (this._content[i].value = n)
            : this._content.push({ key: t, value: n }),
          this
        );
      }),
      (t.prototype.delete = function (t) {
        var n = e(this, t);
        return -1 !== n && (this._content.splice(n, 1), !0);
      }),
      (t.prototype.has = function (t) {
        return e(this, t) > -1;
      }),
      (t.prototype.clear = function () {
        this._content.splice(0, this._content.length);
      }),
      (t.prototype.forEach = function (e, t) {
        this._content.forEach(function (n) {
          e.call(t, n.value, n.key, this);
        }, this);
      }),
      window.Map || t
    );
  }),
  (function (e) {
    define("DS/WebappsUtils/Promise", function () {
      function t(e) {
        return null == e ? e + "" : typeof e;
      }
      function n() {
        for (var e = 0; e < S.length; e++) S[e][0](S[e][1]);
        (S = []), (h = !1);
      }
      function i(e, t) {
        S.push([e, t]), h || ((h = !0), E(n, 0));
      }
      function r(e) {
        var t = e._then;
        e._then = void 0;
        for (var n = 0; n < t.length; n++) p(t[n]);
      }
      function a(e) {
        (e._state = y), r(e);
      }
      function o(e) {
        (e._state = b), r(e);
      }
      function s(e, t) {
        e._state === v && ((e._state = w), (e._data = t), i(a, e));
      }
      function l(e, t) {
        (e !== t && d(e, t)) || s(e, t);
      }
      function c(e, t) {
        e._state === v && ((e._state = w), (e._data = t), i(o, e));
      }
      function u(e, t) {
        function n(e) {
          l(t, e);
        }
        function i(e) {
          c(t, e);
        }
        try {
          e(n, i);
        } catch (e) {
          i(e);
        }
      }
      function d(e, n) {
        var i;
        try {
          if (e === n)
            throw new TypeError(
              "A promises callback cannot return that same promise."
            );
          if (n && ("function" === t(n) || "object" === t(n))) {
            var r = n.then;
            if ("function" === t(r))
              return (
                r.call(
                  n,
                  function (t) {
                    i || ((i = !0), n !== t ? l(e, t) : s(e, t));
                  },
                  function (t) {
                    i || ((i = !0), c(e, t));
                  }
                ),
                !0
              );
          }
        } catch (t) {
          return i || c(e, t), !0;
        }
        return !1;
      }
      function p(e) {
        var n = e.owner,
          i = n._state,
          r = n._data,
          a = e[i],
          o = e.then;
        if ("function" === t(a)) {
          i = y;
          try {
            r = a(r);
          } catch (e) {
            c(o, e);
          }
        }
        d(o, r) || (i === y && l(o, r), i === b && c(o, r));
      }
      function f(e) {
        if ("function" !== t(e))
          throw new TypeError("Promise constructor takes a function argument");
        if (this instanceof f === "false")
          throw new TypeError(
            "Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function."
          );
        (this._then = []), u(e, this);
      }
      var h,
        m = e.Promise,
        g =
          m &&
          "resolve" in m &&
          "reject" in m &&
          "all" in m &&
          "race" in m &&
          (function () {
            var e;
            return (
              new m(function (t) {
                e = t;
              }),
              "function" == typeof e
            );
          })(),
        v = "pending",
        w = "sealed",
        y = "fulfilled",
        b = "rejected",
        E = void 0 !== e.setImmediate ? e.setImmediate : e.setTimeout,
        S = [];
      return (
        (f.prototype = {
          constructor: f,
          _state: v,
          _then: null,
          _data: void 0,
          then: function (e, t) {
            var n = {
              owner: this,
              then: new this.constructor(function () {}),
              fulfilled: e,
              rejected: t,
            };
            return (
              this._state === y || this._state === b
                ? i(p, n)
                : this._then.push(n),
              n.then
            );
          },
          catch: function (e) {
            return this.then(null, e);
          },
        }),
        (f.all = function (e) {
          var n = this;
          if (!Array.isArray(e))
            throw new TypeError("You must pass an array to Promise.all().");
          return new n(function (n, i) {
            for (var r, a = [], o = 0, s = 0; s < e.length; s++)
              (r = e[s]),
                r && "function" === t(r.then)
                  ? r.then(
                      (function (e) {
                        return (
                          o++,
                          function (t) {
                            (a[e] = t), --o || n(a);
                          }
                        );
                      })(s),
                      i
                    )
                  : (a[s] = r);
            o || n(a);
          });
        }),
        (f.race = function (e) {
          var n = this;
          if (!Array.isArray(e))
            throw new TypeError("You must pass an array to Promise.race().");
          return new n(function (n, i) {
            for (var r, a = 0; a < e.length; a++)
              (r = e[a]), r && "function" === t(r.then) ? r.then(n, i) : n(r);
          });
        }),
        (f.resolve = function (e) {
          var n = this;
          return e && "object" === t(e) && e.constructor === n
            ? e
            : new n(function (t) {
                t(e);
              });
        }),
        (f.reject = function (e) {
          return new this(function (t, n) {
            n(e);
          });
        }),
        g ? m : f
      );
    });
  })(this),
  define("DS/WebappsUtils/Utils", function () {
    function e(e) {
      function t() {
        var t = 1e4 * Math.sin(e++);
        return t - Math.floor(t);
      }
      return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (n) {
          var i = e ? t() : Math.random(),
            r = (16 * i) | 0;
          return ("x" === n ? r : (3 & r) | 8).toString(16);
        }
      );
    }
    var t = function e(t) {
        function n(e, t) {
          return (e >>> t) | (e << (32 - t));
        }
        for (
          var i,
            r,
            a = Math.pow,
            o = a(2, 32),
            s = "length",
            l = "",
            c = [],
            u = 8 * t[s],
            d = (e.h = e.h || []),
            p = (e.k = e.k || []),
            f = p[s],
            h = {},
            m = 2;
          f < 64;
          m++
        )
          if (!h[m]) {
            for (i = 0; i < 313; i += m) h[i] = m;
            (d[f] = (a(m, 0.5) * o) | 0), (p[f++] = (a(m, 1 / 3) * o) | 0);
          }
        for (t += ""; (t[s] % 64) - 56; ) t += "\0";
        for (i = 0; i < t[s]; i++) {
          if ((r = t.charCodeAt(i)) >> 8) return;
          c[i >> 2] |= r << (((3 - i) % 4) * 8);
        }
        for (c[c[s]] = (u / o) | 0, c[c[s]] = u, r = 0; r < c[s]; ) {
          var g = c.slice(r, (r += 16)),
            v = d;
          for (d = d.slice(0, 8), i = 0; i < 64; i++) {
            var w = g[i - 15],
              y = g[i - 2],
              b = d[0],
              E = d[4],
              S =
                d[7] +
                (n(E, 6) ^ n(E, 11) ^ n(E, 25)) +
                ((E & d[5]) ^ (~E & d[6])) +
                p[i] +
                (g[i] =
                  i < 16
                    ? g[i]
                    : (g[i - 16] +
                        (n(w, 7) ^ n(w, 18) ^ (w >>> 3)) +
                        g[i - 7] +
                        (n(y, 17) ^ n(y, 19) ^ (y >>> 10))) |
                      0);
            (d = [
              (S +
                ((n(b, 2) ^ n(b, 13) ^ n(b, 22)) +
                  ((b & d[1]) ^ (b & d[2]) ^ (d[1] & d[2])))) |
                0,
            ].concat(d)),
              (d[4] = (d[4] + S) | 0);
          }
          for (i = 0; i < 8; i++) d[i] = (d[i] + v[i]) | 0;
        }
        for (i = 0; i < 8; i++)
          for (r = 3; r + 1; r--) {
            var D = (d[i] >> (8 * r)) & 255;
            l += (D < 16 ? 0 : "") + D.toString(16);
          }
        return l;
      },
      n = {
        uuid: e,
        getUUID: e,
        navigationType: (function () {
          var e,
            t = "unknown";
          return (
            window.performance.navigation &&
              ((e = window.performance.navigation.type),
              (t =
                0 === e
                  ? "navigate"
                  : 1 === e
                  ? "reload"
                  : 2 === e
                  ? "back"
                  : "unknown")),
            t
          );
        })(),
        getChecksum: function () {
          var e = arguments,
            t = String(e[0]),
            n = e[1],
            i = 0,
            r = t.length;
          for (n = n || 305419896; i < r; i++) n += t.charCodeAt(i) * i;
          return Math.abs(parseInt(n, 10)).toString(36);
        },
        beaconPolyfill: function (e, t) {
          var n = window.XMLHttpRequest
            ? new XMLHttpRequest()
            : new window.ActiveXObject("Msxml2.XMLHTTP");
          n.open("POST", e, !1),
            n.setRequestHeader("Content-Type", "text/plain;charset=UTF-8"),
            n.setRequestHeader("Accept", "*/*"),
            n.send(t);
        },
        clientEngine: (function () {
          var e,
            t,
            n,
            i,
            r =
              /(opera|ie|firefox|chrome|version)[\s\/:]([\w\d\.]+)?.*?(safari|version[\s\/:]([\w\d\.]+)|$)/,
            a = /(webkit)[\s\/:]([\w\d\.]+)/,
            o = /(trident)\/.*rv:([\d\.]+)/,
            s = navigator.userAgent ? navigator.userAgent.toLowerCase() : "";
          return (
            (e = s.match(r) || s.match(a) || s.match(o)),
            "ie" === e[1] || "trident" === e[1]
              ? ((t = window.document && document.documentMode),
                (i = t && t.toString()),
                (n = "ie"))
              : ((i = "opera" === e[1] && e[4] ? e[4] : e[2]),
                (n = "version" === e[1] ? e[3] : e[1])),
            { name: n, version: i, userAgent: s }
          );
        })(),
        clientPlatform: (function () {
          var e = /ip(?:ad|od|hone)/,
            t = /webos|wossystem/,
            n = /blackberry/,
            i = /android/,
            r = /mac|win|linux/,
            a = (navigator && navigator.userAgent.toLowerCase()) || "",
            o = ((navigator && navigator.platform.toLowerCase()) || "").split(
              " "
            )[0],
            s = e.test(a)
              ? "ios"
              : t.test(a)
              ? "webos"
              : n.test(a)
              ? "blackberry"
              : i.test(a)
              ? "android"
              : r.test(o)
              ? o
              : "other";
          return {
            name: s,
            mobile:
              "ios" === s ||
              "android" === s ||
              "blackberry" === s ||
              !!a.match(/tablet/),
            resheight: (screen && screen.height) || 0,
            reswidth: (screen && screen.width) || 0,
          };
        })(),
        isVirtualKeyboardOpen: (function () {
          function e() {
            if (!n.clientPlatform.mobile || "ios" === n.clientPlatform.name)
              return !1;
            var e = window.screen.width + "x" + window.screen.height,
              o = i[e];
            return (
              (i[e] = Math.max(i[e] || 0, t.clientHeight)),
              o && t.clientHeight !== a
                ? ((a = t.clientHeight), (r = t.clientHeight < i[e]))
                : r
            );
          }
          var t = (function () {
              try {
                return top.document.documentElement;
              } catch (e) {
                return document.documentElement;
              }
            })(),
            i = {},
            r = !1,
            a = t.clientHeight;
          return setTimeout(e), window.addEventListener("resize", e), e;
        })(),
        copy: function (e) {
          var t,
            n,
            i,
            r = e.contentEditable,
            a = e.readOnly;
          return (
            "ios" === this.clientPlatform.name
              ? ((e.contentEditable = !0),
                (e.readOnly = !0),
                (t = document.createRange()),
                t.selectNodeContents(e),
                (n = window.getSelection()),
                n.removeAllRanges(),
                n.addRange(t),
                e.setSelectionRange(0, e.value.length),
                (e.contentEditable = r),
                (e.readOnly = a))
              : e.select(),
            (i = document.execCommand("copy")),
            e.blur(),
            i
          );
        },
        getCookie: function (e) {
          var t,
            n,
            i = [];
          try {
            i = document.cookie.split("; ");
          } catch (e) {}
          return (
            (t = i.filter(function (t) {
              return t.split("=")[0] === e;
            })),
            t.length && (n = t[0].split("=")[1]),
            n
          );
        },
        setPersistantCookie: function (e, t, n) {
          var i,
            r = new Date();
          r.setFullYear(r.getFullYear() + 1),
            (i = e + "=" + t + "; expires=" + r.toUTCString()),
            n && (i += "; domain=" + n + "; path=/"),
            (document.cookie = i);
        },
        setCookie: function (e, t, n, i) {
          var r = new Date(Date.now() + 6e4 * i),
            a = e + "=" + t;
          n && (a += "; domain=" + n + "; path=/"),
            i && (a += "; expires=" + r.toUTCString()),
            (document.cookie = a);
        },
        removeCookie: function (e, t) {
          var n;
          (n = e + "=undef; expires=" + new Date(0).toUTCString()),
            t && (n += "; domain=" + t + "; path=/"),
            (document.cookie = n);
        },
        debounce: function (e, t, n) {
          var i;
          return function () {
            var r,
              a = this,
              o = arguments,
              s = function () {
                (i = null), n || e.apply(a, o);
              };
            (r = n && !i),
              clearTimeout(i),
              (i = setTimeout(s, t)),
              r && e.apply(a, o);
          };
        },
        throttle: function (e, t, n, i, r) {
          var a,
            o,
            s,
            l = null,
            c = 0,
            u = function () {
              (c = new Date()), (l = null), (s = e.apply(a, o));
            };
          return function () {
            var d = new Date();
            c || n || (c = d);
            var p = t - (d - c);
            return (
              (a = r || this),
              (o = arguments),
              p <= 0
                ? (clearTimeout(l), (l = null), (c = d), (s = e.apply(a, o)))
                : !l && i && (l = setTimeout(u, p)),
              s
            );
          };
        },
        sha256: t,
      };
    return n;
  }),
  (function (e) {
    var t = null,
      n = null,
      i = null,
      r = null,
      a = function (n) {
        return (
          (t = n + ("/" === n[n.length - 1] ? "" : "/")),
          (e.dsDefaultWebappsBaseUrl = t),
          require.config({
            baseUrl: t,
            paths: { DS: ".", vendors: "vendors", UWA: "UWA2/js" },
          }),
          t
        );
      };
    (i = "object" == typeof location ? location.pathname : "/"),
      (r = i.substr(0, i.lastIndexOf("/") + 1) + "../"),
      a(
        "string" == typeof e.dsDefaultWebappsBaseUrl
          ? e.dsDefaultWebappsBaseUrl
          : r
      ),
      "string" == typeof e.dsProxifiedWebappsBaseUrl &&
        (function (e) {
          n = e + ("/" === e[e.length - 1] ? "" : "/");
        })(e.dsProxifiedWebappsBaseUrl),
      define("DS/WebappsUtils/WebappsUtils", function () {
        return {
          getWebappsBaseUrl: function () {
            return t;
          },
          getProxifiedWebappsBaseUrl: function () {
            return n;
          },
          _setWebappsBaseUrl: a,
          getWebappsAssetUrl: function (e, n) {
            return t + e + "/assets/" + n;
          },
        };
      }),
      define("DS/WebappsUtils", ["DS/WebappsUtils/WebappsUtils"], function (e) {
        return e;
      }),
      define(
        "WebappsUtils/WebappsUtils",
        ["DS/WebappsUtils/WebappsUtils"],
        function (e) {
          return e;
        }
      );
  })(this),
  define("DS/WebappsUtils/Inherit", function () {
    function e(e) {
      return null == e ? e + "" : typeof e;
    }
    function t(t) {
      return (
        !("object" !== e(t) || t.nodeType || (null != t && t === t.window)) &&
        !(t.constructor && !n(t.constructor.prototype, "isPrototypeOf"))
      );
    }
    function n(e, t) {
      return null != e && hasOwnProperty.call(e, t);
    }
    function i(e) {
      return function () {
        var i,
          a,
          o,
          s,
          l,
          c,
          u = arguments[0] || {},
          d = 1,
          p = arguments.length,
          f = !1;
        for (
          "boolean" == typeof u && ((f = u), (u = arguments[d] || {}), d++),
            "object" != typeof u && "function" != typeof u && (u = {});
          d < p;
          d++
        )
          if (null != (i = arguments[d]))
            for (a in i)
              if (e || n(i, a)) {
                if (((o = u[a]), (s = i[a]), u === s)) continue;
                f && s && (t(s) || (l = Array.isArray(s)))
                  ? (l
                      ? ((l = !1), (c = o && Array.isArray(o) ? o : []))
                      : (c = o && t(o) ? o : {}),
                    (u[a] = r.extend(f, c, s)))
                  : void 0 !== s && (u[a] = s);
              }
        return u;
      };
    }
    var r = {
      assign: i(),
      extend: i(!0),
      clone: function () {
        var t = arguments[0],
          n = !1;
        return (
          "boolean" === e(t) && ((n = t), (t = arguments[1])),
          r.extend(n, {}, t)
        );
      },
      inherit: function () {
        var e,
          t = arguments[0],
          n = arguments[1];
        return (
          (t.prototype = Object.create(
            "function" == typeof n ? n.prototype : n
          )),
          (t.prototype.constructor = t),
          arguments.length > 2 &&
            ((e = [].slice.call(arguments, 2)),
            e.unshift(t.prototype),
            r.assign.apply(null, e)),
          t
        );
      },
    };
    return r;
  }),
  define("DS/WebappsUtils/Console", function () {
    var e = Array.prototype.slice,
      t = Function.prototype.apply,
      n = function (n, i) {
        return function () {
          "undefined" != typeof console && console[n]
            ? t.call(console[n], console, e.call(arguments))
            : i && i.apply(i, e.call(arguments));
        };
      },
      i = {};
    return (
      (i.log = n("log")),
      (i.info = n("info", i.log)),
      (i.debug = n("debug", i.log)),
      (i.warn = n("warn", i.log)),
      (i.error = n("error", i.log)),
      (i.trace = n("trace", function () {
        i.log(new Error().stack);
      })),
      i
    );
  }),
  define("DS/WebappsUtils/Emitter", function () {
    return {
      on: function (e, t) {
        return (
          (this.listeners = this.listeners || {}),
          e in this.listeners
            ? this.listeners[e].push(t)
            : (this.listeners[e] = [t]),
          this
        );
      },
      trigger: function (e) {
        var t = Array.prototype.slice;
        return (
          (this.listeners = this.listeners || {}),
          e in this.listeners &&
            this.invoke(this.listeners[e], t.call(arguments, 1)),
          "*" in this.listeners && this.invoke(this.listeners["*"], arguments),
          this
        );
      },
      invoke: function (e, t) {
        for (var n = 0, i = e.length; n < i; n++) e[n].apply(this, t);
      },
      off: function (e, t) {
        var n;
        return (
          e in this.listeners &&
            (t
              ? (n = this.listeners[e].indexOf(t)) >= 0 &&
                this.listeners[e].splice(n, 1)
              : (this.listeners[e] = [])),
          this
        );
      },
    };
  }),
  (function (e) {
    define("DS/WebappsUtils/Performance", function () {
      var t = {},
        n = e.performance || {},
        i = [],
        r = +new Date(),
        a = function (e) {
          for (
            var t = e.charAt(0).toUpperCase() + e.slice(1),
              i = [e, "webkit" + t, "ms" + t, "moz" + t],
              r = 0;
            r < i.length;
            r++
          )
            if (n[i[r]]) return n[i[r]].bind(n);
          return null;
        },
        o = [
          "navigationStart",
          "unloadEventStart",
          "unloadEventEnd",
          "redirectStart",
          "redirectEnd",
          "fetchStart",
          "domainLookupStart",
          "domainLookupEnd",
          "connectStart",
          "connectEnd",
          "secureConnectionStart",
          "requestStart",
          "responseStart",
          "responseEnd",
          "domLoading",
          "domInteractive",
          "domContentLoadedEventStart",
          "domContentLoadedEventEnd",
          "domComplete",
          "loadEventStart",
          "loadEventEnd",
        ];
      return (
        (t.navigation = n.navigation || {}),
        (t.timing = n.timing || {}),
        (t.now =
          a("now") ||
          function () {
            return +new Date() - r;
          }),
        (t.mark =
          a("mark") ||
          function (e) {
            if (!e)
              throw new TypeError(
                "Failed to execute 'mark' on 'Performance': 1 argument required, but only 0 present."
              );
            if (-1 !== o.indexOf(e))
              throw new DOMException(
                "Failed to execute 'mark' on 'Performance': " +
                  e +
                  " is part of the PerformanceTiming interface, and cannot be used as a mark name."
              );
            i.push({
              name: e,
              entryType: "mark",
              startTime: t.now(),
              duration: 0,
            });
          }),
        (t.clearMarks =
          a("clearMarks") ||
          function (e) {
            i = i.filter(function (t) {
              return "mark" !== t.entryType || (void 0 !== e && t.name !== e);
            });
          }),
        (t.measure =
          a("measure") ||
          function (n, a, s) {
            var l,
              c,
              u = 0,
              d = 0;
            if (!n)
              throw new TypeError(
                "Failed to execute 'mark' on 'Performance': 1 argument required, but only 0 present."
              );
            if (
              (!t.timing.navigationStart && -1 !== o.indexOf(a)) ||
              -1 !== o.indexOf(s)
            )
              return void e.console.warn(
                "Your browser does not support the navigation timing API."
              );
            if (a) {
              if (
                ((l = i.filter(function (e) {
                  return "mark" === e.entryType && e.name === a;
                })),
                0 === l.length)
              )
                throw new DOMException(
                  "Failed to execute 'measure' on 'Performance': The mark " +
                    a +
                    " does not exist."
                );
              d = l.sort(function (e, t) {
                return e.startTime < t.startTime;
              })[0].startTime;
            } else d = t.timing.navigationStart || r;
            if (s) {
              if (
                ((c = i.filter(function (e) {
                  return "mark" === e.entryType && e.name === s;
                })),
                0 === c.length)
              )
                throw new DOMException(
                  "Failed to execute 'measure' on 'Performance': The mark " +
                    s +
                    " does not exist."
                );
              u = c.sort(function (e, t) {
                return e.startTime < t.startTime;
              })[0].startTime;
            } else u = t.now();
            i.push({
              name: n,
              entryType: "measure",
              startTime: d,
              duration: u - d,
            });
          }),
        (t.clearMeasures =
          a("clearMeasures") ||
          function (e) {
            i = i.filter(function (t) {
              return (
                "measure" !== t.entryType || (void 0 !== e && t.name !== e)
              );
            });
          }),
        (t.getEntries =
          a("getEntries") ||
          function () {
            return i;
          }),
        (t.getEntriesByType =
          a("getEntriesByType") ||
          function (e) {
            return i.filter(function (t) {
              return t.entryType === e;
            });
          }),
        (t.getEntriesByName =
          a("getEntriesByName") ||
          function (e) {
            return i.filter(function (t) {
              return t.name === e;
            });
          }),
        (!e.performance ||
          ("function" != typeof e.performance.mark &&
            "function" != typeof e.performance.measure)) &&
          (e.performance
            ? Object.keys(t).forEach(function (n) {
                e.performance[n] || (e.performance[n] = t[n]);
              })
            : (e.performance = t)),
        t
      );
    });
  })(this),
  define("DS/Usage/Events", [], function () {
    "use strict";
    return {
      counter: { increment: "counter:increment" },
      timer: { start: "timer:start", stop: "timer:stop" },
      tracker: {
        creation: "tracker:creation",
        update: "tracker:update",
        getContent: "tracker:get-content",
        gotContent: "tracker:got-content",
      },
      page: {
        view: "page:view",
        event: "page:event",
        interaction: "page:interaction",
      },
      data: { register: "data:register" },
      error: { runtime: "error:runtime" },
    };
  }),
  define(
    "DS/Usage/UsageShardingManager",
    [
      "UWA/Utils",
      "DS/WebappsUtils/Inherit",
      "DS/WebappsUtils/Utils",
      "DS/Usage/Events",
    ],
    function (e, t, n, i) {
      var r,
        a = { themKey: "webapp", user: null, newVisitor: !1 },
        o = {
          scrollable: function (e) {
            return (
              e.clientWidth < e.offsetWidth ||
              "scroll" == e.style.overflowY ||
              "auto" == e.style.overflowY
            );
          },
          getAppScrollableContainer: function (e) {
            for (var t, n = e; n; )
              n.clientHeight < n.scrollHeight && this.scrollable(n) && (t = n),
                (n = n.parentNode);
            return t || document.body;
          },
          containerHeight: function (e) {
            var t = Math.max(e.scrollHeight, e.offsetHeight, e.clientHeight);
            return e.getBoundingClientRect().top + t;
          },
          containerWidth: function (e) {
            var t = Math.max(e.scrollWidth, e.offsetWidth, e.clientWidth);
            return e.getBoundingClientRect().left + t;
          },
          viewportTop: function (e) {
            return e.scrollTop;
          },
          viewportBottom: function (e) {
            return (
              this.viewportTop(e) +
              (window.innerHeight ||
                document.documentElement.clientHeight ||
                document.body.clientHeight)
            );
          },
          viewportLeft: function (e) {
            return e.scrollLeft;
          },
          viewportRight: function (e) {
            return (
              this.viewportLeft(e) +
              (window.innerWidth ||
                document.documentElement.clientWidth ||
                document.body.clientWidth)
            );
          },
        },
        s = function (e) {
          var t = e.topic.split(":");
          return (t = t.length > 2 ? t.slice(0, 2) : t).join(":");
        },
        l = {
          flatten: function (e, n) {
            e[n] &&
              Array.isArray(e[n]) &&
              (e[n].forEach(function (n) {
                t.extend(e, n);
              }),
              delete e[n]);
          },
          flattenData: function (e) {
            e &&
              e.data &&
              [
                "persDimPages",
                "persValPages",
                "persDimPageEvents",
                "persValPageEvents",
              ].forEach(function (t) {
                l.flatten(e.data, t);
              });
          },
          clearData: function (e) {
            (e.sessions = []),
              (e.pages = e.pages.length ? [e.pages.pop()] : []),
              (e.interactions = []),
              (e.events = []),
              (e.errors = []);
          },
          pruneData: function (e) {
            (e.sessions = e.sessions.slice(0, 100)),
              (e.pages = e.pages.slice(0, 100)),
              (e.interactions = e.interactions.slice(0, 100)),
              (e.events = e.events.slice(0, 100));
          },
          getValidURL:
            ((r = document.createElement("a")),
            function (e) {
              return (r.href = e), r.href;
            }),
          models: [
            { internal: "pages", payload: "usage" },
            { internal: "events", payload: "usage" },
            { internal: "interactions", payload: "usage" },
            { internal: "errors", payload: "usage" },
            { internal: "sessions", payload: "cusage" },
          ],
          getLastPageID: function (e) {
            return (
              (e.pages &&
                e.pages.length > 0 &&
                e.pages[e.pages.length - 1] &&
                e.pages[e.pages.length - 1].id) ||
              ""
            );
          },
          getUTMParameters: function () {
            var t = {},
              n = e.parseQuery(window.location.search);
            return n
              ? ([
                  "utm_source",
                  "utm_medium",
                  "utm_campaign",
                  "utm_term",
                  "utm_content",
                ].forEach(function (e) {
                  n.hasOwnProperty(e) && (t[e] = n[e]);
                }),
                t)
              : t;
          },
          getTopDomain: function () {
            var t,
              n = e.parseUrl(window.location).domain,
              i = "";
            if (
              !/^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/g.test(
                n
              ) &&
              "localhost" !== n
            ) {
              for (t = n.split("."); t.length > 2; ) t.shift();
              i = t.join(".");
            }
            return i;
          },
          getSession: function (e) {
            return (
              (e = e || {}),
              t.extend(e, {
                r_schemaName: "wasession",
                r_schemaVersion: "1.10",
                r_service: a.service,
                r_timestamp: Date.now(),
                them: a.themKey,
                sessionID: n.getCookie("_dss"),
                user: n.getCookie("_dsu"),
                casUser: a.authUser ? n.sha256(a.authUser) : "",
                referrer: document.referrer,
                newVisitor: a.newVisitor,
                serverhostname: window.location.hostname,
                locale: navigator.language || "en-US",
                enginename: n.clientEngine.name,
                userAgent: n.clientEngine.userAgent,
                engineversion: n.clientEngine.version,
                userAgent: navigator.userAgent,
                platformname: n.clientPlatform.name,
                platformmobile: n.clientPlatform.mobile,
                resheight: n.clientPlatform.resheight,
                reswidth: n.clientPlatform.reswidth,
                navigationtype: n.navigationType,
                startTime: a.navigationStart,
              }),
              t.extend(e, l.getUTMParameters()),
              e
            );
          },
        },
        c = {
          enableUserInteraction: function (e) {
            var r = e && e.target,
              a = r && o.getAppScrollableContainer(r),
              s = {};
            if (
              r &&
              l.getLastPageID(this) &&
              ("scroll" !== e.type || a === r)
            ) {
              t.extend(s, {
                interactionid: n.uuid(),
                action: e.type,
                viewportTop: o.viewportTop(a),
                viewportBottom: o.viewportBottom(a),
                viewportLeft: o.viewportLeft(a),
                viewportRight: o.viewportRight(a),
                pageWitdh: o.containerWidth(a),
                pageHeight: o.containerHeight(a),
              }),
                ["id", "className", "tagName"].forEach(function (e) {
                  var t = r[e];
                  t &&
                    ("string" == typeof t || t instanceof String) &&
                    (s[e] = r[e]);
                }),
                ["clientX", "clientY"].forEach(function (t) {
                  e[t] && (s[t] = e[t]),
                    e.touches &&
                      e.touches[0] &&
                      e.touches[0][t] &&
                      (s[t] = e.touches[0][t]);
                });
              var c = { topic: i.page.interaction, data: s },
                u = { data: JSON.stringify(c) };
              this.readMessage(u);
            }
          },
          enableErrorTracking: function (e) {
            var t,
              n = { topic: i.error.runtime, data: {} };
            e.filename && (n.data.source = e.filename),
              e.message && (n.data.message = e.message),
              e.colno && (n.data.column = e.colno),
              e.lineno && (n.data.line = e.lineno),
              Object.keys(n.data).length &&
                ((t = { data: JSON.stringify(n) }), this.readMessage(t));
          },
          manageCookies: function () {
            var e,
              i = n.getCookie("_dsus"),
              r = n.getCookie("_dsut"),
              o = l.getTopDomain();
            i && n.setCookie("_dss", i, o, a.sessionDuration),
              r && n.setPersistantCookie("_dsu", r, o),
              (e = n.getCookie("_dss")),
              (!a.seed && e) || (e = n.uuid(a.seed)),
              n.setCookie("_dss", e, o, a.sessionDuration),
              (a.user = n.getCookie("_dsu")),
              a.user
                ? (a.newVisitor = !1)
                : ((a.user = n.uuid()), (a.newVisitor = !0)),
              n.setPersistantCookie("_dsu", a.user, o),
              a.sessionID !== e &&
                ((a.seed = null),
                (a.navigationStart = Date.now()),
                (a.sessionID = e),
                l.clearData(this),
                this.pages.length &&
                  t.extend(this.pages[0], {
                    id: n.uuid(),
                    sessionID: a.sessionID,
                    browseTime: Date.now() - a.navigationStart,
                    r_timestamp: Date.now(),
                    timestamp: Date.now(),
                  }),
                this.sessions.push(l.getSession()),
                this.send());
          },
        };
      return {
        sessions: [],
        pages: [],
        interactions: [],
        events: [],
        errors: [],
        init: function () {
          var e,
            i = this,
            r = !1;
          e =
            "string" == typeof arguments[0]
              ? {
                  uploadPath: arguments[0],
                  service: arguments[1],
                  user: arguments[2],
                }
              : arguments[0] || {};
          try {
            window.top.location.protocol;
          } catch (e) {
            r = !0;
          }
          r ||
            (t.extend(a, {
              uploadPath: l.getValidURL(e.uploadPath),
              sessionDuration: e.sessionDuration || 30,
              sendDebounceRate: e.sendDebounceRate || 2500,
              userActivityThrottleRate: e.userActivityThrottleRate || 5e3,
              userInteractionThrottleRate: e.userInteractionThrottleRate || 1e3,
              userInteractionDebounceRate: e.userInteractionDebounceRate || 500,
              service: e.service,
              authUser: (e.user && e.user.login) || "",
              hostname: e.hostname || window.location.hostname,
              them: e.them || "webapp",
              seed: e.seed || null,
              navigationStart:
                !window.performance ||
                (window.performance && !window.performance.timing)
                  ? Date.now()
                  : window.performance.timing.navigationStart,
            }),
            window.addEventListener("beforeunload", i.send.bind(i, "beacon")),
            window.addEventListener("message", i.readMessage.bind(i)),
            window.addEventListener(
              "mousemove",
              n.throttle(
                c.manageCookies.bind(i),
                a.userActivityThrottleRate,
                !1,
                !0
              )
            ),
            [
              "click",
              "dblclick",
              "contextmenu",
              "touchstart",
              "touchend",
            ].forEach(function (e) {
              window.addEventListener(
                e,
                n.throttle(
                  c.enableUserInteraction.bind(i),
                  a.userInteractionThrottleRate,
                  !0
                ),
                !0
              );
            }),
            ["scroll", "mousemove", "touchmove"].forEach(function (e) {
              window.addEventListener(
                e,
                n.debounce(
                  c.enableUserInteraction.bind(i),
                  a.userInteractionDebounceRate
                ),
                !0
              );
            }),
            window.addEventListener("error", c.enableErrorTracking.bind(i)),
            (i.sendDebounced = n.debounce(i.send.bind(i), a.sendDebounceRate)),
            c.manageCookies.call(i));
        },
        readMessage: function (e) {
          var r = {};
          try {
            (r = JSON.parse(e.data)), l.flattenData(r);
          } catch (e) {}
          if (!r.forAPI && r.topic) {
            switch ((e.stopPropagation && e.stopPropagation(), s(r))) {
              case i.page.view:
                t.extend(r.data, {
                  r_schemaName: "wapage",
                  r_schemaVersion: "1.07",
                  r_service: a.service,
                  r_timestamp: Date.now(),
                  serverhostname: window.location.hostname,
                  id: n.uuid(),
                  sessionID: n.getCookie("_dss"),
                  browseTime: Date.now() - a.navigationStart,
                }),
                  r.data.timestamp && delete r.data.timestamp,
                  this.pages.push(r.data);
                break;
              case i.page.event:
                t.extend(r.data, {
                  r_schemaName: "waevent",
                  r_schemaVersion: "1.07",
                  r_service: a.service,
                  r_timestamp: Date.now(),
                  id: n.uuid(),
                  sessionID: n.getCookie("_dss"),
                  sessionpageID: l.getLastPageID(this),
                }),
                  r.data.timestamp && delete r.data.timestamp,
                  this.events.push(r.data);
                break;
              case i.page.interaction:
                t.extend(r.data, {
                  r_schemaName: "wainteraction",
                  r_schemaVersion: "1.07",
                  r_service: a.service,
                  r_timestamp: Date.now(),
                  sessionID: n.getCookie("_dss"),
                  sessionpageID: l.getLastPageID(this),
                }),
                  this.interactions.push(r.data);
                break;
              case i.data.register:
                t.extend(r.data, {
                  r_schemaName: "wasession",
                  r_schemaVersion: "1.10",
                  r_service: a.service,
                  r_timestamp: Date.now(),
                  sessionID: n.getCookie("_dss"),
                }),
                  this.sessions.push(r.data);
                break;
              case i.error.runtime:
                t.extend(r.data, {
                  r_schemaName: "waerror",
                  r_schemaVersion: "1.04",
                  r_service: a.service,
                  r_timestamp: Date.now(),
                  id: n.uuid(),
                  sessionID: n.getCookie("_dss"),
                  sessionpageID: l.getLastPageID(this),
                  level: "error",
                  category: "runtime",
                }),
                  this.errors.push(r.data);
            }
            l.pruneData(this), r.sync ? this.send() : this.sendDebounced();
          }
        },
        send: function () {
          var e,
            t = new FormData(),
            i =
              arguments.length > 0 && void 0 !== arguments[0]
                ? arguments[0]
                : "xhr",
            r = this,
            o = 0;
          l.models.forEach(function (e) {
            r[e.internal].forEach(function (n) {
              t.append(e.payload + o, JSON.stringify(n)), o++;
            });
          }),
            o &&
              ("beacon" === i
                ? navigator.sendBeacon
                  ? navigator.sendBeacon(a.uploadPath, t)
                  : n.beaconPolyfill(a.uploadPath, t)
                : ((e = new XMLHttpRequest()).open("POST", a.uploadPath),
                  e.send(t)),
              l.clearData(this));
        },
      };
    }
  ),
  define(
    "DS/Usage/UsageManager",
    ["DS/Usage/UsageShardingManager"],
    function (e) {
      return {
        init: function () {
          var t;
          (t =
            "string" == typeof arguments[0]
              ? {
                  uploadPath: arguments[0],
                  service: arguments[1],
                  user: arguments[2],
                }
              : arguments[0] || {}),
            e.init(t);
        },
      };
    }
  ),
  define(
    "DS/Usage/AbstractTracker",
    [
      "DS/WebappsUtils/Inherit",
      "DS/WebappsUtils/Promise",
      "DS/WebappsUtils/Emitter",
      "DS/Usage/Events",
    ],
    function (e, t, n, i) {
      "use strict";
      function r(e) {
        this.name = e;
      }
      return (
        e.inherit(r, {
          set: function (e, t) {
            var r;
            "object" == typeof e ? ((r = e), (t = void 0)) : ((r = {})[e] = t),
              Object.keys(r).length > 0 &&
                ((r.name = this.name),
                (r.type = this.type),
                n.trigger(i.tracker.update, r));
          },
          get: function () {
            var e = this;
            return new t(function (t) {
              var r = i.tracker.gotContent + ":" + e.name,
                a = i.tracker.getContent + ":" + e.name;
              n.on(r, function (i) {
                n.off(r), i && i.data && i.data.name === e.name && t(i.data);
              }),
                n.trigger(a, { name: e.name });
            });
          },
        }),
        r
      );
    }
  ),
  define(
    "DS/Usage/ActionTracker",
    [
      "DS/WebappsUtils/Inherit",
      "DS/Usage/AbstractTracker",
      "DS/WebappsUtils/Console",
    ],
    function (e, t, n) {
      "use strict";
      function i(e) {
        n.warn(
          "Module DS/Usage/ActionTracker is deprecated. Use DS/Usage/TrackerAPI#trackPageEvent instead"
        ),
          t.call(this, e),
          (this.type = "action");
      }
      return e.inherit(i, t), i;
    }
  ),
  define(
    "DS/Usage/TrackerAPI",
    [
      "DS/WebappsUtils/Inherit",
      "DS/WebappsUtils/Emitter",
      "DS/Usage/ActionTracker",
      "DS/Usage/Events",
    ],
    function (e, t, n, i) {
      "use strict";
      function r(e) {
        return "string" == typeof e || e instanceof String;
      }
      function a(e) {
        return "number" == typeof e && isFinite(e);
      }
      function o(e) {
        return e && "object" == typeof e && e.constructor === Object;
      }
      function s(e) {
        return "function" == typeof e;
      }
      function l(e) {
        var t = !0;
        if (!o(e)) return !1;
        if (0 == Object.keys(e).length) return !1;
        if (Object.keys(e).length >= 20) return !1;
        for (var n in e)
          e.hasOwnProperty(n) &&
            (/^(pd|pv)[0-9]+$/.test(n) || (t = !1), r(e[n]) || (t = !1));
        return t;
      }
      function c(e) {
        var t = !0;
        if (!o(e)) return !1;
        if (0 == Object.keys(e).length) return !1;
        if (Object.keys(e).length >= 20) return !1;
        for (var n in e)
          e.hasOwnProperty(n) &&
            (/^(pd|pv)[0-9]+$/.test(n) || (t = !1), a(e[n]) || (t = !1));
        return t;
      }
      var u = !1,
        d = (function () {
          var e = window.location,
            t = window.top.location;
          try {
            t.protocol;
          } catch (e) {
            u = !0;
          }
          return u
            ? {}
            : {
                current:
                  e.protocol + "//" + e.hostname + (e.port ? ":" + e.port : ""),
                parent:
                  t.protocol + "//" + t.hostname + (t.port ? ":" + t.port : ""),
              };
        })(),
        p = window.top,
        f = {
          getTracker: function () {
            var t,
              r,
              a = arguments,
              o = a[0],
              s = a[1],
              l = a[2];
            if (
              (void 0 === l && "object" == typeof s
                ? ((l = s), (s = o), (o = "action"))
                : void 0 === l && "string" == typeof s
                ? (l = {})
                : void 0 === s &&
                  void 0 === l &&
                  ("string" == typeof o
                    ? ((s = o), (o = "action"), (l = {}))
                    : ((s = (r = e.clone(o)).name),
                      (o = r.type),
                      (l = r.options || {}))),
              void 0 === s)
            )
              throw new TypeError("No tracker name specified");
            switch (o) {
              case "action":
                var c = l;
                (c.name = s),
                  this.notify(c, i.tracker.creation),
                  (t = new n(s));
            }
            return t;
          },
          trackPageView: function (e, t, n, a, f) {
            var h = arguments[0];
            if (
              (o(h) &&
                ((e = h.pageURL),
                (t = h.pageTitle),
                (n = h.persDim),
                (a = h.pageLanguage),
                (f = { persVal: h.persVal, appID: h.appID, tenant: h.tenant })),
              !u && e && r(e))
            ) {
              var m = {
                origin: d.current,
                topic: i.page.view,
                sync: !!h.callback,
                data: {
                  url: e,
                  timestamp: Date.now(),
                  pagelg: a || (window.dsLang ? window.dsLang : ""),
                },
              };
              t && r(t) && (m.data.title = t),
                n && l(n) && (m.data.persDimPages = [n]),
                f &&
                  o(f) &&
                  c(f.persVal) &&
                  (m.data.persValPages = [f.persVal]),
                f && o(f) && f.appID && (m.data.appID = f.appID),
                f && o(f) && f.tenant && (m.data.tenant = f.tenant),
                p.postMessage && p.postMessage(JSON.stringify(m), d.parent),
                s(h.callback) && setTimeout(h.callback, 0);
            }
          },
          trackPageEvent: function (e, t, n, f, h, m) {
            var g = arguments[0];
            if (
              (o(g) &&
                ((e = g.eventCategory),
                (t = g.eventAction),
                (n = g.eventLabel),
                (f = g.eventValue),
                (h = g.persDim),
                (m = { persVal: g.persVal, appID: g.appID, tenant: g.tenant })),
              !u && e && r(e) && t && r(t))
            ) {
              var v = {
                origin: d.current,
                topic: i.page.event,
                sync: !!g.callback,
                data: { category: e, action: t, timestamp: Date.now() },
              };
              n && r(n) && (v.data.label = n),
                f && a(f) && (v.data.value = f),
                h && l(h) && (v.data.persDimPageEvents = [h]),
                m &&
                  o(m) &&
                  c(m.persVal) &&
                  (v.data.persValPageEvents = [m.persVal]),
                m && o(m) && m.appID && (v.data.appID = m.appID),
                m && o(m) && m.tenant && (v.data.tenant = m.tenant),
                p.postMessage && p.postMessage(JSON.stringify(v), d.parent),
                s(g.callback) && setTimeout(g.callback, 0);
            }
          },
          registerData: function () {
            var e,
              t,
              n = arguments;
            !u &&
              n.length &&
              (1 === n.length && "object" == typeof n[0]
                ? (e = n[0])
                : n.length > 1 && ((e = {})[n[0]] = n[1]),
              e &&
                ((t = { origin: d.current, topic: i.data.register, data: e }),
                p.postMessage && p.postMessage(JSON.stringify(t), d.parent)));
          },
          notify: function (e, t) {
            if (!u) {
              var n = { origin: d.current, topic: t, data: e };
              p.postMessage && p.postMessage(JSON.stringify(n), d.parent);
            }
          },
          init: function () {
            if (!u) {
              var n = this;
              this._initiated ||
                ((this._initiated = !0),
                t.on("*", function (t, i) {
                  i.forAPI || n.notify(e.clone(i), t);
                }),
                window.addEventListener("message", function (e) {
                  var n = {};
                  try {
                    n = JSON.parse(e.data);
                  } catch (e) {}
                  n.forAPI && (e.stopPropagation(), t.trigger(n.topic, n));
                }));
            }
          },
        };
      return f.init(), f;
    }
  ),
  define(
    "DS/W3DPassport/dsp/utils/createTrackerPageView",
    ["DS/Usage/TrackerAPI"],
    function (e) {
      "use strict";
      return function (t, n, i, r, a) {
        void 0 !== t.trackerJs &&
          "true" === t.trackerJs &&
          window.location === window.parent.location &&
          e.trackPageView({
            pageURL: n,
            pageTitle: i,
            persDim: r,
            callback: a,
          });
      };
    }
  ),
  define(
    "DS/W3DPassport/dsp/utils/createTrackerPageEvent",
    ["DS/Usage/TrackerAPI"],
    function (e) {
      "use strict";
      return function (t, n, i, r, a) {
        void 0 !== t.trackerJs &&
          "true" === t.trackerJs &&
          window.location === window.parent.location &&
          e.trackPageEvent({
            eventCategory: n,
            eventAction: i,
            persDim: r,
            callback: a,
          });
      };
    }
  ),
  define(
    "DS/W3DPassport/login/rememberLastUser",
    ["UWA/Element"],
    function (e) {
      "use strict";
      function t(e) {
        var t = e.lastIndexOf("@"),
          n = e.indexOf(".", t);
        return t > 0 && n < 0;
      }
      function n(e) {
        var t = e.lastIndexOf("@");
        return { username: e.substr(0, t), siteId: e.substr(t + 1) };
      }
      return (
        (window.passport_multisite = "@@is_multi_site@@"),
        {
          load: function (t, n, i, r) {
            var a,
              o = e.getElement.call(t, '[name="username"]'),
              s = e.getElement.call(t, '[class="login-legend"]');
            if (
              localStorage &&
              (a = localStorage.getItem("lastUser")) &&
              o &&
              ((o.value = a),
              o.getClosest(".field").addClassName("hidden"),
              (n.textContent = a),
              i.removeClassName("hidden"),
              i.getElement(".username").setText(a),
              r.removeClassName("hidden"),
              s.addClassName("hidden"),
              "true" === window.passport_multisite)
            ) {
              e.getElement.call(t, '[name="siteid"]').value =
                localStorage.getItem("x3ds_siteid");
            }
          },
          save: function (i) {
            var r = e.getElement.call(i, '[name="username"]');
            if (localStorage) {
              var a = r.value,
                o = "";
              try {
                if ("true" === window.passport_multisite && t(a)) {
                  var s = n(a);
                  (a = s.username), (o = s.siteId);
                }
                localStorage.setItem("lastUser", a),
                  localStorage.setItem("x3ds_siteid", o);
              } catch (e) {
                console.log("localStorage unavailable");
              }
            }
          },
        }
      );
    }
  ),
  define(
    "DS/W3DPassport/login/randomBackground",
    ["DS/W3DPassport/dsp/UWA"],
    function (e) {
      "use strict";
      return function () {
        var t = Math.floor(12 * Math.random()) + 1;
        e.Element.addClassName.call(
          document.body,
          "ifwe-background ifwe-background-" + t
        );
      };
    }
  ),
  define(
    "DS/W3DPassport/login",
    [
      "DS/W3DPassport/dsp/config/login",
      "DS/W3DPassport/dsp/i18n/i18n",
      "DS/W3DPassport/dsp/UWA",
      "DS/W3DPassport/dsp/form/createLoginForm",
      "DS/W3DPassport/dsp/form/createRegisterForm",
      "DS/W3DPassport/dsp/utils/addMessagesToPanel",
      "DS/W3DPassport/dsp/utils/loginTicketTimeoutMsg",
      "DS/W3DPassport/dsp/utils/createLanguagePicker",
      "DS/W3DPassport/dsp/utils/createPasswordInformationPanel",
      "DS/W3DPassport/dsp/utils/hightlightBackendError",
      "DS/W3DPassport/dsp/utils/createTrackerPageView",
      "DS/W3DPassport/dsp/utils/createTrackerPageEvent",
      "DS/W3DPassport/login/rememberLastUser",
      "DS/W3DPassport/login/randomBackground",
      "DS/Usage/UsageManager",
      "DS/WebappsUtils/Utils",
    ],
    function (e, t, n, i, r, a, o, s, l, c, u, d, p, f, h, m) {
      "use strict";
      function g(e) {
        var t,
          n,
          i = e.getElements(".field");
        for (n = i.length; n-- > 0; )
          (t = i[n]),
            t.hasClassName("required") ||
              t.hasClassName("display") ||
              t.remove();
      }
      function v(t) {
        (P = r(
          n.extend(e, {
            fields: t,
            url: e.registerUrl,
            apiUrlCountries: e.apiUrlCountries,
          })
        )),
          P.getContent().addClassName("hidden");
        var i = P.getContent();
        void 0 !== e.trackerJs &&
          "true" === e.trackerJs &&
          window.location === window.parent.location &&
          i.addEvent("submit", function () {
            var t = function () {
              i.submit();
            };
            arguments[0].preventDefault();
            var n = {
              pd1: "regular",
              pd2: m.sha256(
                document.querySelector("[name='data[email]']").value
              ),
              pd3: m.sha256(
                document.querySelector("[name='data[username]']").value
              ),
            };
            d(e, "register", "registerTentative", n, t);
          }),
          g(P.getContent()),
          _.addContent(P),
          _.addContent(
            n.createElement("div", {
              class: "register-links hidden",
              html: {
                tag: "a",
                href: "#login",
                "data-dsp-i18n": "commons.action.logIn",
                text: "Log in",
                events: {
                  click: function (t) {
                    u(e, "LogIn", "LoginPage"), n.Event.stop(t), b();
                  },
                },
              },
            })
          ),
          l("password", P.getContent(), e),
          void 0 !== e.captchaRegister &&
            "true" === e.captchaRegister &&
            ("legacyCaptcha" === e.captchaType
              ? ((U = new n.dsp.component.CaptchaDisplay({
                  captchaImgUrl: e.captchaImgUrl,
                })),
                U.inject(P.getContent().getElement(".field.captcha"), "before"))
              : require(["recaptcha"], function (t) {
                  (U = n.createElement("div", {
                    class: "g-recaptcha",
                    "data-sitekey": e.captchaClientId,
                  })),
                    U.inject(
                      P.getContent().querySelector(".uwa-submit"),
                      "before"
                    );
                })),
          c(P.getContent(), e);
      }
      function w(e, t) {
        var i = n.Element.getElement.call(document.body, e);
        null !== i &&
          (t && !i.hasClassName("hidden")
            ? i.addClassName("hidden")
            : !t && i.hasClassName("hidden") && i.removeClassName("hidden"));
      }
      function y() {
        (window.location.hash = "register"),
          T
            ? (w(".login-form", !0),
              w(".login-links", !0),
              w(".register-form", !1),
              w(".register-links", !1),
              w(".welcome", !0),
              t.translateDocument())
            : ((T = !0),
              n.Json.request(e.getRegisterFormUrl, {
                onComplete: function (e) {
                  v(e),
                    w(".login-form", !0),
                    w(".login-links", !0),
                    w(".register-form", !1),
                    w(".register-links", !1),
                    w(".welcome", !0),
                    t.translateDocument();
                },
              }));
      }
      function b() {
        (window.location.hash = "login"),
          w(".login-form", !1),
          w(".login-links", !1),
          w(".register-form", !0),
          w(".register-links", !0),
          localStorage.getItem("lastUser") &&
            (w(".welcome", !1), w(".login-legend", !0));
      }
      var E,
        S,
        D,
        _,
        x,
        C,
        k,
        P,
        U,
        A,
        W,
        I,
        T,
        N,
        B,
        L,
        H = [];
      if (
        (void 0 !== e.trackerJs &&
          "true" === e.trackerJs &&
          window.location === window.parent.location &&
          h.init("/upload", "passport"),
        t.init(e),
        u(e, "LogIn", "LoginPage"),
        e.errorMsgs.length > 0)
      )
        for (var M = 0; M < e.errorMsgs.length; M++)
          "error.captcha.wrong" == e.errorMsgs[M].code
            ? (d(e, "captcha", "captchaFailure"), d(e, "LogIn", "logInFailure"))
            : "user.already.exists" == e.errorMsgs[M].code
            ? (u(e, "register", "RegisterPage"),
              d(e, "register", "registerFailure"))
            : e.errorMsgs[M].code.indexOf("error.register") >= 0
            ? (u(e, "register", "RegisterPage"),
              d(e, "register", "registerFailure"))
            : d(e, "LogIn", "logInFailure");
      f();
      var B = n.createElement("div", { id: "outer" }),
        R = n.createElement("div", { id: "middle" }),
        L = n.createElement("div", { id: "inner" });
      if (
        ((E = new n.dsp.component.Panel({
          className: "panel-form login-panel",
          title: "",
        })),
        (x = n.createElement("div", {
          class: "dddxp-logo-container",
          html: [{ tag: "span", class: "dddxp-logo" }],
        })),
        L.addContent(x),
        navigator.cookieEnabled ||
          ((N = n.createElement("div", {
            class: "dddxp-cookies",
            "data-dsp-i18n": "commons.label.cookies",
            text: "Please enable cookies on your browser to login.",
          })),
          N.inject(document.body)),
        window.addEventListener("hashchange", function (e) {
          var t = window.location.hash.substr(1);
          "register" === t ? y() : "login" === t && b();
        }),
        e.doNotShowLoginForm ||
          ((C = i({
            url: e.url,
            loginTicket: e.lt,
            displayCaptcha: e.needsCaptcha,
            username: e.username,
            authorizeRememberMe: e.authorizeRememberMe,
            footnoteNls: e.footnoteNls,
            needHelpUrl: e.needHelpUrl,
            captchaType: e.captchaType,
          })),
          a(e, E),
          (A = n.createElement("div", {
            class: "lt-expired-error hidden",
            html: [
              {
                tag: "p",
                class: "lt-expired-msg",
                "data-dsp-i18n": "login.ticket.expired",
              },
              {
                tag: "a",
                class: "lt-expired-btn",
                "data-dsp-i18n": "commons.action.log.again",
                tabindex: 6,
                events: {
                  click: function (t) {
                    u(e, "LogIn", "LoginPage"),
                      n.Event.stop(t),
                      window.location.reload(!1);
                  },
                },
              },
            ],
          })),
          H.push({
            tag: "div",
            id: "iAmNotLink",
            class: "hidden",
            html: [
              {
                tag: "a",
                html: [
                  {
                    tag: "span",
                    "data-dsp-i18n": "commons.login.iamnot",
                    text: "I am not",
                  },
                  { tag: "span", text: " " },
                  { tag: "span", class: "username" },
                ],
                href: "#",
                events: {
                  click: function (e) {
                    n.Event.stop(e),
                      w(".field.username", !1),
                      w("#iAmNotLink", !0),
                      w(".welcome", !0),
                      w(".login-legend", !1),
                      n.Element.getElement
                        .call(document.body, ".username.uwa-input")
                        .focus(),
                      (n.Element.getElement.call(
                        document.body,
                        ".username.uwa-input"
                      ).value = ""),
                      localStorage && localStorage.removeItem("lastUser");
                  },
                },
              },
            ],
          }),
          (T = !1),
          e.registerUrl &&
            ((_ = n.createElement("div")),
            E.addContentElement(_),
            H.push({
                tag:"div",
                html:[
                    {
                        tag: "span",
                        text: "本平台为内网非涉密平台，严禁处理、传输国家秘密",
                    }
                ]
            })
            ,
            H.push({
              tag: "div",
              html: [
                {
                  tag: "span",
                  text: "Not registered?",
                  "data-dsp-i18n": "commons.not.registered",
                  class: "loginSimpleText",
                },
                {
                  tag: "a",
                  class: "go-register",
                  "data-dsp-i18n": "commons.account.create",
                  text: "Create your 3DEXPERIENCE ID",
                  tabindex: 7,
                  events: {
                    click: function (t) {
                      u(e, "register", "RegisterPage"), n.Event.stop(t), y();
                    },
                  },
                },
              ],
            })),
          !0 === e.allowRemoteLogin &&
            H.push({
              tag: "div",
              html: [
                {
                  tag: "a",
                  "data-dsp-i18n": "commons.live.connect",
                  text: "Live connect",
                  tabindex: 7,
                  href: e.liveConnectUrl,
                },
              ],
            }),
          e.forgotPasswordUrl &&
            H.push({
              tag: "div",
              html: [
                {
                  tag: "a",
                  href: e.forgotPasswordUrl,
                  "data-dsp-i18n": "commons.password.forgot",
                  text: "Forgot my password",
                  tabindex: 8,
                },
              ],
            }),
          void 0 !== e.footnoteNls &&
            "" !== e.footnoteNls &&
            null !== e.footnoteNls &&
            H.push({
              tag: "div",
              class: "login-asterisk-div",
              html: [
                {
                  tag: "span",
                  text: "",
                  "data-dsp-i18n": e.footnoteNls,
                  class: "login-asterisk-text",
                },
              ],
            }),
          (I = n.createElement("div")),
          void 0 !== e.needHelpUrl &&
            "" !== e.needHelpUrl &&
            null !== e.needHelpUrl &&
            (I = n.createElement("div", {
              class: "login-needhelp-div",
              html: [
                {
                  tag: "a",
                  href: e.needHelpUrl,
                  "data-dsp-i18n": "commons.login.needhelp",
                  text: "Need help?",
                  target: "Support",
                },
              ],
            })),
          (W = new n.Element("div", { class: "login-links", html: H })),
          e.needsCaptcha &&
            ("legacyCaptcha" === e.captchaType
              ? ((U = new n.dsp.component.CaptchaDisplay({
                  captchaImgUrl: e.captchaImgUrl,
                })),
                U.inject(C.getContent().getElement(".field.captcha"), "before"))
              : require(["recaptcha"], function (t) {
                  (U = n.createElement("div", {
                    class: "g-recaptcha",
                    "data-sitekey": e.captchaClientId,
                  })),
                    U.inject(
                      C.getContent().getElement(".field.password"),
                      "after"
                    );
                }))),
        (k = C.getContent()),
        !e.doNotShowLoginForm)
      ) {
        E.addContentElement(
          n.createElement("div", {
            class: "welcome",
            html: [
              {
                tag: "span",
                "data-dsp-i18n": "commons.login.welcome",
                text: "Welcome",
              },
              { tag: "span", text: " " },
              { tag: "span", id: "userNameWelcome" },
              {
                tag: "span",
                text: "本平台为内网非涉密平台，严禁处理、传输国家秘密",
              },
            ],
          })
        ),
          E.addContentElement(C),
          E.addContentElement(A),
          E.addContentElement(W);
        var O = n.createElement("div", { class: "centerDiv" }),
          j =
            void 0 !== e.samlButtonText &&
            void 0 !== e.samlButtonLabel &&
            void 0 !== e.samlActivationUrl;
        (void 0 !== e.availableSN.gplus ||
          void 0 !== e.availableSN.fb ||
          (e.samlEnabled && j)) &&
          require(["DS/W3DPassport/login/socialLinks"], function (i) {
            O.addContent(
              n.createElement("div", {
                class: "orSeparator",
                html: {
                  tag: "span",
                  "data-dsp-i18n": "commons.or.separator",
                  text: t.translate("commons.or.separator"),
                },
              })
            ),
              1 == e.samlEnabled &&
                j &&
                ((S = n.createElement("div", { class: "connectPanel" })),
                (D = new n.createElement("button", {
                  type: "button",
                  class: "medium uwa-button uwa-input connectButton",
                  text: e.samlButtonText,
                  "data-dsp-i18n": e.samlButtonLabel,
                  events: {
                    click: function (t) {
                      window.location.href = e.samlActivationUrl;
                    },
                  },
                })),
                S.addContent(D),
                O.addContent(S)),
              (void 0 === e.availableSN.gplus && void 0 === e.availableSN.fb) ||
                O.addContent(
                  i.createSocialButtons({
                    facebook: e.availableSN.fb,
                    gplus: e.availableSN.gplus,
                  })
                );
          }),
          E.addContentElement(O),
          E.addContentElement(s(t, e.cookieDomain)),
          E.addContentElement(I);
      }
      if (
        (n.Element.getElement
          .call(E.getContent(), ".welcome")
          .addClassName("hidden"),
        "#reset_login" !== location.hash &&
          p.load(
            k,
            n.Element.getElement.call(E.getContent(), "#userNameWelcome"),
            n.Element.getElement.call(E.getContent(), "#iAmNotLink"),
            n.Element.getElement.call(E.getContent(), ".welcome")
          ),
        k.addEvent("submit", function () {
          if (
            void 0 !== e.trackerJs &&
            "true" === e.trackerJs &&
            window.location === window.parent.location
          ) {
            if (
              (arguments[0].preventDefault(),
              k.getElement(".username input").value.includes("@"))
            )
              var t = {
                pd1: "regular",
                pd2: m.sha256(k.getElement(".username input").value),
              };
            else
              var t = {
                pd1: "regular",
                pd2: "",
                pd3: m.sha256(k.getElement(".username input").value),
              };
            var n = function () {
              k.submit();
            };
            e.needsCaptcha && d(e, "captcha", "captchaTentative", t, n),
              d(e, "LogIn", "logInTentative", t, n);
          }
          p.save(k);
        }),
        L.addContent(E),
        R.addContent(L),
        B.addContent(R),
        B.inject(document.body),
        "#register" === location.hash)
      ) {
        var z = n.Element.getElement.call(
          document.body,
          ".login-links .go-register"
        );
        null !== z && z.triggerEvent("click");
      }
      e.doNotShowLoginForm || o(e.loginTicketUrl), t.translateDocument();
    }
  );
