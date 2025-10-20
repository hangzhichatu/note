/*global define */
define("DS/W3DPassport/dsp/utils/areRequiredOptionsMissing", [
	
], function () {
    "use strict";

    /** Function to check that a configuration object have the given properties.
     * If all the properties are setted it returns true otherwise it returns
     * false.
     *
     * @param {Object} config The object with the configuration properties.
     * @param {Array} properties An array of the names of the required properties.
     * @returns {Boolean} Return if all the properties as set, false otherwise.
     */
    return function (config, properties) {
        var result = false,
            i;

        i = properties.length;
        while (!(result) && (0 < i)) {
            i -= 1;
            if (undefined === config[properties[i]]) {
                result = true;
            }
        }
        return result;
    };
});

var CryptoJS=CryptoJS||function(e,t){var n={},r=n.lib={},i=function(){},s=r.Base={extend:function(e){i.prototype=this;var t=new i;return e&&t.mixIn(e),t.hasOwnProperty("init")||(t.init=function(){t.$super.init.apply(this,arguments)}),t.init.prototype=t,t.$super=this,t},create:function(){var e=this.extend();return e.init.apply(e,arguments),e},init:function(){},mixIn:function(e){for(var t in e)e.hasOwnProperty(t)&&(this[t]=e[t]);e.hasOwnProperty("toString")&&(this.toString=e.toString)},clone:function(){return this.init.prototype.extend(this)}},o=r.WordArray=s.extend({init:function(e,n){e=this.words=e||[],this.sigBytes=n!=t?n:4*e.length},toString:function(e){return(e||a).stringify(this)},concat:function(e){var t=this.words,n=e.words,r=this.sigBytes;e=e.sigBytes,this.clamp();if(r%4)for(var i=0;i<e;i++)t[r+i>>>2]|=(n[i>>>2]>>>24-8*(i%4)&255)<<24-8*((r+i)%4);else if(65535<n.length)for(i=0;i<e;i+=4)t[r+i>>>2]=n[i>>>2];else t.push.apply(t,n);return this.sigBytes+=e,this},clamp:function(){var t=this.words,n=this.sigBytes;t[n>>>2]&=4294967295<<32-8*(n%4),t.length=e.ceil(n/4)},clone:function(){var e=s.clone.call(this);return e.words=this.words.slice(0),e},random:function(t){for(var n=[],r=0;r<t;r+=4)n.push(4294967296*e.random()|0);return new o.init(n,t)}}),u=n.enc={},a=u.Hex={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++){var i=t[r>>>2]>>>24-8*(r%4)&255;n.push((i>>>4).toString(16)),n.push((i&15).toString(16))}return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r+=2)n[r>>>3]|=parseInt(e.substr(r,2),16)<<24-4*(r%8);return new o.init(n,t/2)}},f=u.Latin1={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++)n.push(String.fromCharCode(t[r>>>2]>>>24-8*(r%4)&255));return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r++)n[r>>>2]|=(e.charCodeAt(r)&255)<<24-8*(r%4);return new o.init(n,t)}},l=u.Utf8={stringify:function(e){try{return decodeURIComponent(escape(f.stringify(e)))}catch(t){throw Error("Malformed UTF-8 data")}},parse:function(e){return f.parse(unescape(encodeURIComponent(e)))}},c=r.BufferedBlockAlgorithm=s.extend({reset:function(){this._data=new o.init,this._nDataBytes=0},_append:function(e){"string"==typeof e&&(e=l.parse(e)),this._data.concat(e),this._nDataBytes+=e.sigBytes},_process:function(t){var n=this._data,r=n.words,i=n.sigBytes,s=this.blockSize,u=i/(4*s),u=t?e.ceil(u):e.max((u|0)-this._minBufferSize,0);t=u*s,i=e.min(4*t,i);if(t){for(var a=0;a<t;a+=s)this._doProcessBlock(r,a);a=r.splice(0,t),n.sigBytes-=i}return new o.init(a,i)},clone:function(){var e=s.clone.call(this);return e._data=this._data.clone(),e},_minBufferSize:0});r.Hasher=c.extend({cfg:s.extend(),init:function(e){this.cfg=this.cfg.extend(e),this.reset()},reset:function(){c.reset.call(this),this._doReset()},update:function(e){return this._append(e),this._process(),this},finalize:function(e){return e&&this._append(e),this._doFinalize()},blockSize:16,_createHelper:function(e){return function(t,n){return(new e.init(n)).finalize(t)}},_createHmacHelper:function(e){return function(t,n){return(new h.HMAC.init(e,n)).finalize(t)}}});var h=n.algo={};return n}(Math);(function(e){function t(e,t,n,r,i,s,o){return e=e+(t&n|~t&r)+i+o,(e<<s|e>>>32-s)+t}function n(e,t,n,r,i,s,o){return e=e+(t&r|n&~r)+i+o,(e<<s|e>>>32-s)+t}function r(e,t,n,r,i,s,o){return e=e+(t^n^r)+i+o,(e<<s|e>>>32-s)+t}function i(e,t,n,r,i,s,o){return e=e+(n^(t|~r))+i+o,(e<<s|e>>>32-s)+t}for(var s=CryptoJS,o=s.lib,u=o.WordArray,a=o.Hasher,o=s.algo,f=[],l=0;64>l;l++)f[l]=4294967296*e.abs(e.sin(l+1))|0;o=o.MD5=a.extend({_doReset:function(){this._hash=new u.init([1732584193,4023233417,2562383102,271733878])},_doProcessBlock:function(e,s){for(var o=0;16>o;o++){var u=s+o,a=e[u];e[u]=(a<<8|a>>>24)&16711935|(a<<24|a>>>8)&4278255360}var o=this._hash.words,u=e[s+0],a=e[s+1],l=e[s+2],c=e[s+3],h=e[s+4],d=e[s+5],v=e[s+6],g=e[s+7],y=e[s+8],b=e[s+9],w=e[s+10],E=e[s+11],S=e[s+12],x=e[s+13],T=e[s+14],N=e[s+15],C=o[0],k=o[1],L=o[2],A=o[3],C=t(C,k,L,A,u,7,f[0]),A=t(A,C,k,L,a,12,f[1]),L=t(L,A,C,k,l,17,f[2]),k=t(k,L,A,C,c,22,f[3]),C=t(C,k,L,A,h,7,f[4]),A=t(A,C,k,L,d,12,f[5]),L=t(L,A,C,k,v,17,f[6]),k=t(k,L,A,C,g,22,f[7]),C=t(C,k,L,A,y,7,f[8]),A=t(A,C,k,L,b,12,f[9]),L=t(L,A,C,k,w,17,f[10]),k=t(k,L,A,C,E,22,f[11]),C=t(C,k,L,A,S,7,f[12]),A=t(A,C,k,L,x,12,f[13]),L=t(L,A,C,k,T,17,f[14]),k=t(k,L,A,C,N,22,f[15]),C=n(C,k,L,A,a,5,f[16]),A=n(A,C,k,L,v,9,f[17]),L=n(L,A,C,k,E,14,f[18]),k=n(k,L,A,C,u,20,f[19]),C=n(C,k,L,A,d,5,f[20]),A=n(A,C,k,L,w,9,f[21]),L=n(L,A,C,k,N,14,f[22]),k=n(k,L,A,C,h,20,f[23]),C=n(C,k,L,A,b,5,f[24]),A=n(A,C,k,L,T,9,f[25]),L=n(L,A,C,k,c,14,f[26]),k=n(k,L,A,C,y,20,f[27]),C=n(C,k,L,A,x,5,f[28]),A=n(A,C,k,L,l,9,f[29]),L=n(L,A,C,k,g,14,f[30]),k=n(k,L,A,C,S,20,f[31]),C=r(C,k,L,A,d,4,f[32]),A=r(A,C,k,L,y,11,f[33]),L=r(L,A,C,k,E,16,f[34]),k=r(k,L,A,C,T,23,f[35]),C=r(C,k,L,A,a,4,f[36]),A=r(A,C,k,L,h,11,f[37]),L=r(L,A,C,k,g,16,f[38]),k=r(k,L,A,C,w,23,f[39]),C=r(C,k,L,A,x,4,f[40]),A=r(A,C,k,L,u,11,f[41]),L=r(L,A,C,k,c,16,f[42]),k=r(k,L,A,C,v,23,f[43]),C=r(C,k,L,A,b,4,f[44]),A=r(A,C,k,L,S,11,f[45]),L=r(L,A,C,k,N,16,f[46]),k=r(k,L,A,C,l,23,f[47]),C=i(C,k,L,A,u,6,f[48]),A=i(A,C,k,L,g,10,f[49]),L=i(L,A,C,k,T,15,f[50]),k=i(k,L,A,C,d,21,f[51]),C=i(C,k,L,A,S,6,f[52]),A=i(A,C,k,L,c,10,f[53]),L=i(L,A,C,k,w,15,f[54]),k=i(k,L,A,C,a,21,f[55]),C=i(C,k,L,A,y,6,f[56]),A=i(A,C,k,L,N,10,f[57]),L=i(L,A,C,k,v,15,f[58]),k=i(k,L,A,C,x,21,f[59]),C=i(C,k,L,A,h,6,f[60]),A=i(A,C,k,L,E,10,f[61]),L=i(L,A,C,k,l,15,f[62]),k=i(k,L,A,C,b,21,f[63]);o[0]=o[0]+C|0,o[1]=o[1]+k|0,o[2]=o[2]+L|0,o[3]=o[3]+A|0},_doFinalize:function(){var t=this._data,n=t.words,r=8*this._nDataBytes,i=8*t.sigBytes;n[i>>>5]|=128<<24-i%32;var s=e.floor(r/4294967296);n[(i+64>>>9<<4)+15]=(s<<8|s>>>24)&16711935|(s<<24|s>>>8)&4278255360,n[(i+64>>>9<<4)+14]=(r<<8|r>>>24)&16711935|(r<<24|r>>>8)&4278255360,t.sigBytes=4*(n.length+1),this._process(),t=this._hash,n=t.words;for(r=0;4>r;r++)i=n[r],n[r]=(i<<8|i>>>24)&16711935|(i<<24|i>>>8)&4278255360;return t},clone:function(){var e=a.clone.call(this);return e._hash=this._hash.clone(),e}}),s.MD5=a._createHelper(o),s.HmacMD5=a._createHmacHelper(o)})(Math);

define("libs/cryptojs/md5", function(){});

var CryptoJS=CryptoJS||function(e,t){var n={},r=n.lib={},i=function(){},s=r.Base={extend:function(e){i.prototype=this;var t=new i;return e&&t.mixIn(e),t.hasOwnProperty("init")||(t.init=function(){t.$super.init.apply(this,arguments)}),t.init.prototype=t,t.$super=this,t},create:function(){var e=this.extend();return e.init.apply(e,arguments),e},init:function(){},mixIn:function(e){for(var t in e)e.hasOwnProperty(t)&&(this[t]=e[t]);e.hasOwnProperty("toString")&&(this.toString=e.toString)},clone:function(){return this.init.prototype.extend(this)}},o=r.WordArray=s.extend({init:function(e,n){e=this.words=e||[],this.sigBytes=n!=t?n:4*e.length},toString:function(e){return(e||a).stringify(this)},concat:function(e){var t=this.words,n=e.words,r=this.sigBytes;e=e.sigBytes,this.clamp();if(r%4)for(var i=0;i<e;i++)t[r+i>>>2]|=(n[i>>>2]>>>24-8*(i%4)&255)<<24-8*((r+i)%4);else if(65535<n.length)for(i=0;i<e;i+=4)t[r+i>>>2]=n[i>>>2];else t.push.apply(t,n);return this.sigBytes+=e,this},clamp:function(){var t=this.words,n=this.sigBytes;t[n>>>2]&=4294967295<<32-8*(n%4),t.length=e.ceil(n/4)},clone:function(){var e=s.clone.call(this);return e.words=this.words.slice(0),e},random:function(t){for(var n=[],r=0;r<t;r+=4)n.push(4294967296*e.random()|0);return new o.init(n,t)}}),u=n.enc={},a=u.Hex={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++){var i=t[r>>>2]>>>24-8*(r%4)&255;n.push((i>>>4).toString(16)),n.push((i&15).toString(16))}return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r+=2)n[r>>>3]|=parseInt(e.substr(r,2),16)<<24-4*(r%8);return new o.init(n,t/2)}},f=u.Latin1={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++)n.push(String.fromCharCode(t[r>>>2]>>>24-8*(r%4)&255));return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r++)n[r>>>2]|=(e.charCodeAt(r)&255)<<24-8*(r%4);return new o.init(n,t)}},l=u.Utf8={stringify:function(e){try{return decodeURIComponent(escape(f.stringify(e)))}catch(t){throw Error("Malformed UTF-8 data")}},parse:function(e){return f.parse(unescape(encodeURIComponent(e)))}},c=r.BufferedBlockAlgorithm=s.extend({reset:function(){this._data=new o.init,this._nDataBytes=0},_append:function(e){"string"==typeof e&&(e=l.parse(e)),this._data.concat(e),this._nDataBytes+=e.sigBytes},_process:function(t){var n=this._data,r=n.words,i=n.sigBytes,s=this.blockSize,u=i/(4*s),u=t?e.ceil(u):e.max((u|0)-this._minBufferSize,0);t=u*s,i=e.min(4*t,i);if(t){for(var a=0;a<t;a+=s)this._doProcessBlock(r,a);a=r.splice(0,t),n.sigBytes-=i}return new o.init(a,i)},clone:function(){var e=s.clone.call(this);return e._data=this._data.clone(),e},_minBufferSize:0});r.Hasher=c.extend({cfg:s.extend(),init:function(e){this.cfg=this.cfg.extend(e),this.reset()},reset:function(){c.reset.call(this),this._doReset()},update:function(e){return this._append(e),this._process(),this},finalize:function(e){return e&&this._append(e),this._doFinalize()},blockSize:16,_createHelper:function(e){return function(t,n){return(new e.init(n)).finalize(t)}},_createHmacHelper:function(e){return function(t,n){return(new h.HMAC.init(e,n)).finalize(t)}}});var h=n.algo={};return n}(Math);(function(e){for(var t=CryptoJS,n=t.lib,r=n.WordArray,i=n.Hasher,n=t.algo,s=[],o=[],u=function(e){return 4294967296*(e-(e|0))|0},a=2,f=0;64>f;){var l;e:{l=a;for(var c=e.sqrt(l),h=2;h<=c;h++)if(!(l%h)){l=!1;break e}l=!0}l&&(8>f&&(s[f]=u(e.pow(a,.5))),o[f]=u(e.pow(a,1/3)),f++),a++}var p=[],n=n.SHA256=i.extend({_doReset:function(){this._hash=new r.init(s.slice(0))},_doProcessBlock:function(e,t){for(var n=this._hash.words,r=n[0],i=n[1],s=n[2],u=n[3],a=n[4],f=n[5],l=n[6],c=n[7],h=0;64>h;h++){if(16>h)p[h]=e[t+h]|0;else{var d=p[h-15],v=p[h-2];p[h]=((d<<25|d>>>7)^(d<<14|d>>>18)^d>>>3)+p[h-7]+((v<<15|v>>>17)^(v<<13|v>>>19)^v>>>10)+p[h-16]}d=c+((a<<26|a>>>6)^(a<<21|a>>>11)^(a<<7|a>>>25))+(a&f^~a&l)+o[h]+p[h],v=((r<<30|r>>>2)^(r<<19|r>>>13)^(r<<10|r>>>22))+(r&i^r&s^i&s),c=l,l=f,f=a,a=u+d|0,u=s,s=i,i=r,r=d+v|0}n[0]=n[0]+r|0,n[1]=n[1]+i|0,n[2]=n[2]+s|0,n[3]=n[3]+u|0,n[4]=n[4]+a|0,n[5]=n[5]+f|0,n[6]=n[6]+l|0,n[7]=n[7]+c|0},_doFinalize:function(){var t=this._data,n=t.words,r=8*this._nDataBytes,i=8*t.sigBytes;return n[i>>>5]|=128<<24-i%32,n[(i+64>>>9<<4)+14]=e.floor(r/4294967296),n[(i+64>>>9<<4)+15]=r,t.sigBytes=4*n.length,this._process(),this._hash},clone:function(){var e=i.clone.call(this);return e._hash=this._hash.clone(),e}});t.SHA256=i._createHelper(n),t.HmacSHA256=i._createHmacHelper(n)})(Math);

define("libs/cryptojs/sha256", function(){});

var CryptoJS=CryptoJS||function(e,t){var n={},r=n.lib={},i=function(){},s=r.Base={extend:function(e){i.prototype=this;var t=new i;return e&&t.mixIn(e),t.hasOwnProperty("init")||(t.init=function(){t.$super.init.apply(this,arguments)}),t.init.prototype=t,t.$super=this,t},create:function(){var e=this.extend();return e.init.apply(e,arguments),e},init:function(){},mixIn:function(e){for(var t in e)e.hasOwnProperty(t)&&(this[t]=e[t]);e.hasOwnProperty("toString")&&(this.toString=e.toString)},clone:function(){return this.init.prototype.extend(this)}},o=r.WordArray=s.extend({init:function(e,n){e=this.words=e||[],this.sigBytes=n!=t?n:4*e.length},toString:function(e){return(e||a).stringify(this)},concat:function(e){var t=this.words,n=e.words,r=this.sigBytes;e=e.sigBytes,this.clamp();if(r%4)for(var i=0;i<e;i++)t[r+i>>>2]|=(n[i>>>2]>>>24-8*(i%4)&255)<<24-8*((r+i)%4);else if(65535<n.length)for(i=0;i<e;i+=4)t[r+i>>>2]=n[i>>>2];else t.push.apply(t,n);return this.sigBytes+=e,this},clamp:function(){var t=this.words,n=this.sigBytes;t[n>>>2]&=4294967295<<32-8*(n%4),t.length=e.ceil(n/4)},clone:function(){var e=s.clone.call(this);return e.words=this.words.slice(0),e},random:function(t){for(var n=[],r=0;r<t;r+=4)n.push(4294967296*e.random()|0);return new o.init(n,t)}}),u=n.enc={},a=u.Hex={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++){var i=t[r>>>2]>>>24-8*(r%4)&255;n.push((i>>>4).toString(16)),n.push((i&15).toString(16))}return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r+=2)n[r>>>3]|=parseInt(e.substr(r,2),16)<<24-4*(r%8);return new o.init(n,t/2)}},f=u.Latin1={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++)n.push(String.fromCharCode(t[r>>>2]>>>24-8*(r%4)&255));return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r++)n[r>>>2]|=(e.charCodeAt(r)&255)<<24-8*(r%4);return new o.init(n,t)}},l=u.Utf8={stringify:function(e){try{return decodeURIComponent(escape(f.stringify(e)))}catch(t){throw Error("Malformed UTF-8 data")}},parse:function(e){return f.parse(unescape(encodeURIComponent(e)))}},c=r.BufferedBlockAlgorithm=s.extend({reset:function(){this._data=new o.init,this._nDataBytes=0},_append:function(e){"string"==typeof e&&(e=l.parse(e)),this._data.concat(e),this._nDataBytes+=e.sigBytes},_process:function(t){var n=this._data,r=n.words,i=n.sigBytes,s=this.blockSize,u=i/(4*s),u=t?e.ceil(u):e.max((u|0)-this._minBufferSize,0);t=u*s,i=e.min(4*t,i);if(t){for(var a=0;a<t;a+=s)this._doProcessBlock(r,a);a=r.splice(0,t),n.sigBytes-=i}return new o.init(a,i)},clone:function(){var e=s.clone.call(this);return e._data=this._data.clone(),e},_minBufferSize:0});r.Hasher=c.extend({cfg:s.extend(),init:function(e){this.cfg=this.cfg.extend(e),this.reset()},reset:function(){c.reset.call(this),this._doReset()},update:function(e){return this._append(e),this._process(),this},finalize:function(e){return e&&this._append(e),this._doFinalize()},blockSize:16,_createHelper:function(e){return function(t,n){return(new e.init(n)).finalize(t)}},_createHmacHelper:function(e){return function(t,n){return(new h.HMAC.init(e,n)).finalize(t)}}});var h=n.algo={};return n}(Math);(function(e){var t=CryptoJS,n=t.lib,r=n.Base,i=n.WordArray,t=t.x64={};t.Word=r.extend({init:function(e,t){this.high=e,this.low=t}}),t.WordArray=r.extend({init:function(t,n){t=this.words=t||[],this.sigBytes=n!=e?n:8*t.length},toX32:function(){for(var e=this.words,t=e.length,n=[],r=0;r<t;r++){var s=e[r];n.push(s.high),n.push(s.low)}return i.create(n,this.sigBytes)},clone:function(){for(var e=r.clone.call(this),t=e.words=this.words.slice(0),n=t.length,i=0;i<n;i++)t[i]=t[i].clone();return e}})})(),function(){function e(){return i.create.apply(i,arguments)}for(var t=CryptoJS,n=t.lib.Hasher,r=t.x64,i=r.Word,s=r.WordArray,r=t.algo,o=[e(1116352408,3609767458),e(1899447441,602891725),e(3049323471,3964484399),e(3921009573,2173295548),e(961987163,4081628472),e(1508970993,3053834265),e(2453635748,2937671579),e(2870763221,3664609560),e(3624381080,2734883394),e(310598401,1164996542),e(607225278,1323610764),e(1426881987,3590304994),e(1925078388,4068182383),e(2162078206,991336113),e(2614888103,633803317),e(3248222580,3479774868),e(3835390401,2666613458),e(4022224774,944711139),e(264347078,2341262773),e(604807628,2007800933),e(770255983,1495990901),e(1249150122,1856431235),e(1555081692,3175218132),e(1996064986,2198950837),e(2554220882,3999719339),e(2821834349,766784016),e(2952996808,2566594879),e(3210313671,3203337956),e(3336571891,1034457026),e(3584528711,2466948901),e(113926993,3758326383),e(338241895,168717936),e(666307205,1188179964),e(773529912,1546045734),e(1294757372,1522805485),e(1396182291,2643833823),e(1695183700,2343527390),e(1986661051,1014477480),e(2177026350,1206759142),e(2456956037,344077627),e(2730485921,1290863460),e(2820302411,3158454273),e(3259730800,3505952657),e(3345764771,106217008),e(3516065817,3606008344),e(3600352804,1432725776),e(4094571909,1467031594),e(275423344,851169720),e(430227734,3100823752),e(506948616,1363258195),e(659060556,3750685593),e(883997877,3785050280),e(958139571,3318307427),e(1322822218,3812723403),e(1537002063,2003034995),e(1747873779,3602036899),e(1955562222,1575990012),e(2024104815,1125592928),e(2227730452,2716904306),e(2361852424,442776044),e(2428436474,593698344),e(2756734187,3733110249),e(3204031479,2999351573),e(3329325298,3815920427),e(3391569614,3928383900),e(3515267271,566280711),e(3940187606,3454069534),e(4118630271,4000239992),e(116418474,1914138554),e(174292421,2731055270),e(289380356,3203993006),e(460393269,320620315),e(685471733,587496836),e(852142971,1086792851),e(1017036298,365543100),e(1126000580,2618297676),e(1288033470,3409855158),e(1501505948,4234509866),e(1607167915,987167468),e(1816402316,1246189591)],u=[],a=0;80>a;a++)u[a]=e();r=r.SHA512=n.extend({_doReset:function(){this._hash=new s.init([new i.init(1779033703,4089235720),new i.init(3144134277,2227873595),new i.init(1013904242,4271175723),new i.init(2773480762,1595750129),new i.init(1359893119,2917565137),new i.init(2600822924,725511199),new i.init(528734635,4215389547),new i.init(1541459225,327033209)])},_doProcessBlock:function(e,t){for(var n=this._hash.words,r=n[0],i=n[1],s=n[2],a=n[3],f=n[4],l=n[5],c=n[6],n=n[7],h=r.high,d=r.low,v=i.high,m=i.low,g=s.high,b=s.low,w=a.high,E=a.low,S=f.high,x=f.low,T=l.high,N=l.low,C=c.high,k=c.low,L=n.high,A=n.low,O=h,M=d,_=v,D=m,P=g,H=b,B=w,j=E,F=S,I=x,q=T,R=N,U=C,z=k,W=L,X=A,V=0;80>V;V++){var $=u[V];if(16>V)var J=$.high=e[t+2*V]|0,K=$.low=e[t+2*V+1]|0;else{var J=u[V-15],K=J.high,Q=J.low,J=(K>>>1|Q<<31)^(K>>>8|Q<<24)^K>>>7,Q=(Q>>>1|K<<31)^(Q>>>8|K<<24)^(Q>>>7|K<<25),G=u[V-2],K=G.high,Y=G.low,G=(K>>>19|Y<<13)^(K<<3|Y>>>29)^K>>>6,Y=(Y>>>19|K<<13)^(Y<<3|K>>>29)^(Y>>>6|K<<26),K=u[V-7],Z=K.high,et=u[V-16],tt=et.high,et=et.low,K=Q+K.low,J=J+Z+(K>>>0<Q>>>0?1:0),K=K+Y,J=J+G+(K>>>0<Y>>>0?1:0),K=K+et,J=J+tt+(K>>>0<et>>>0?1:0);$.high=J,$.low=K}var Z=F&q^~F&U,et=I&R^~I&z,$=O&_^O&P^_&P,nt=M&D^M&H^D&H,Q=(O>>>28|M<<4)^(O<<30|M>>>2)^(O<<25|M>>>7),G=(M>>>28|O<<4)^(M<<30|O>>>2)^(M<<25|O>>>7),Y=o[V],rt=Y.high,it=Y.low,Y=X+((I>>>14|F<<18)^(I>>>18|F<<14)^(I<<23|F>>>9)),tt=W+((F>>>14|I<<18)^(F>>>18|I<<14)^(F<<23|I>>>9))+(Y>>>0<X>>>0?1:0),Y=Y+et,tt=tt+Z+(Y>>>0<et>>>0?1:0),Y=Y+it,tt=tt+rt+(Y>>>0<it>>>0?1:0),Y=Y+K,tt=tt+J+(Y>>>0<K>>>0?1:0),K=G+nt,$=Q+$+(K>>>0<G>>>0?1:0),W=U,X=z,U=q,z=R,q=F,R=I,I=j+Y|0,F=B+tt+(I>>>0<j>>>0?1:0)|0,B=P,j=H,P=_,H=D,_=O,D=M,M=Y+K|0,O=tt+$+(M>>>0<Y>>>0?1:0)|0}d=r.low=d+M,r.high=h+O+(d>>>0<M>>>0?1:0),m=i.low=m+D,i.high=v+_+(m>>>0<D>>>0?1:0),b=s.low=b+H,s.high=g+P+(b>>>0<H>>>0?1:0),E=a.low=E+j,a.high=w+B+(E>>>0<j>>>0?1:0),x=f.low=x+I,f.high=S+F+(x>>>0<I>>>0?1:0),N=l.low=N+R,l.high=T+q+(N>>>0<R>>>0?1:0),k=c.low=k+z,c.high=C+U+(k>>>0<z>>>0?1:0),A=n.low=A+X,n.high=L+W+(A>>>0<X>>>0?1:0)},_doFinalize:function(){var e=this._data,t=e.words,n=8*this._nDataBytes,r=8*e.sigBytes;return t[r>>>5]|=128<<24-r%32,t[(r+128>>>10<<5)+30]=Math.floor(n/4294967296),t[(r+128>>>10<<5)+31]=n,e.sigBytes=4*t.length,this._process(),this._hash.toX32()},clone:function(){var e=n.clone.call(this);return e._hash=this._hash.clone(),e},blockSize:32}),t.SHA512=n._createHelper(r),t.HmacSHA512=n._createHmacHelper(r)}();

define("libs/cryptojs/sha512", function(){});

var CryptoJS=CryptoJS||function(e,t){var n={},r=n.lib={},i=function(){},s=r.Base={extend:function(e){i.prototype=this;var t=new i;return e&&t.mixIn(e),t.hasOwnProperty("init")||(t.init=function(){t.$super.init.apply(this,arguments)}),t.init.prototype=t,t.$super=this,t},create:function(){var e=this.extend();return e.init.apply(e,arguments),e},init:function(){},mixIn:function(e){for(var t in e)e.hasOwnProperty(t)&&(this[t]=e[t]);e.hasOwnProperty("toString")&&(this.toString=e.toString)},clone:function(){return this.init.prototype.extend(this)}},o=r.WordArray=s.extend({init:function(e,n){e=this.words=e||[],this.sigBytes=n!=t?n:4*e.length},toString:function(e){return(e||a).stringify(this)},concat:function(e){var t=this.words,n=e.words,r=this.sigBytes;e=e.sigBytes,this.clamp();if(r%4)for(var i=0;i<e;i++)t[r+i>>>2]|=(n[i>>>2]>>>24-8*(i%4)&255)<<24-8*((r+i)%4);else if(65535<n.length)for(i=0;i<e;i+=4)t[r+i>>>2]=n[i>>>2];else t.push.apply(t,n);return this.sigBytes+=e,this},clamp:function(){var t=this.words,n=this.sigBytes;t[n>>>2]&=4294967295<<32-8*(n%4),t.length=e.ceil(n/4)},clone:function(){var e=s.clone.call(this);return e.words=this.words.slice(0),e},random:function(t){for(var n=[],r=0;r<t;r+=4)n.push(4294967296*e.random()|0);return new o.init(n,t)}}),u=n.enc={},a=u.Hex={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++){var i=t[r>>>2]>>>24-8*(r%4)&255;n.push((i>>>4).toString(16)),n.push((i&15).toString(16))}return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r+=2)n[r>>>3]|=parseInt(e.substr(r,2),16)<<24-4*(r%8);return new o.init(n,t/2)}},f=u.Latin1={stringify:function(e){var t=e.words;e=e.sigBytes;for(var n=[],r=0;r<e;r++)n.push(String.fromCharCode(t[r>>>2]>>>24-8*(r%4)&255));return n.join("")},parse:function(e){for(var t=e.length,n=[],r=0;r<t;r++)n[r>>>2]|=(e.charCodeAt(r)&255)<<24-8*(r%4);return new o.init(n,t)}},l=u.Utf8={stringify:function(e){try{return decodeURIComponent(escape(f.stringify(e)))}catch(t){throw Error("Malformed UTF-8 data")}},parse:function(e){return f.parse(unescape(encodeURIComponent(e)))}},c=r.BufferedBlockAlgorithm=s.extend({reset:function(){this._data=new o.init,this._nDataBytes=0},_append:function(e){"string"==typeof e&&(e=l.parse(e)),this._data.concat(e),this._nDataBytes+=e.sigBytes},_process:function(t){var n=this._data,r=n.words,i=n.sigBytes,s=this.blockSize,u=i/(4*s),u=t?e.ceil(u):e.max((u|0)-this._minBufferSize,0);t=u*s,i=e.min(4*t,i);if(t){for(var a=0;a<t;a+=s)this._doProcessBlock(r,a);a=r.splice(0,t),n.sigBytes-=i}return new o.init(a,i)},clone:function(){var e=s.clone.call(this);return e._data=this._data.clone(),e},_minBufferSize:0});r.Hasher=c.extend({cfg:s.extend(),init:function(e){this.cfg=this.cfg.extend(e),this.reset()},reset:function(){c.reset.call(this),this._doReset()},update:function(e){return this._append(e),this._process(),this},finalize:function(e){return e&&this._append(e),this._doFinalize()},blockSize:16,_createHelper:function(e){return function(t,n){return(new e.init(n)).finalize(t)}},_createHmacHelper:function(e){return function(t,n){return(new h.HMAC.init(e,n)).finalize(t)}}});var h=n.algo={};return n}(Math);(function(e){var t=CryptoJS,n=t.lib,r=n.Base,i=n.WordArray,t=t.x64={};t.Word=r.extend({init:function(e,t){this.high=e,this.low=t}}),t.WordArray=r.extend({init:function(t,n){t=this.words=t||[],this.sigBytes=n!=e?n:8*t.length},toX32:function(){for(var e=this.words,t=e.length,n=[],r=0;r<t;r++){var s=e[r];n.push(s.high),n.push(s.low)}return i.create(n,this.sigBytes)},clone:function(){for(var e=r.clone.call(this),t=e.words=this.words.slice(0),n=t.length,i=0;i<n;i++)t[i]=t[i].clone();return e}})})(),function(e){for(var t=CryptoJS,n=t.lib,r=n.WordArray,i=n.Hasher,s=t.x64.Word,n=t.algo,o=[],u=[],a=[],f=1,l=0,c=0;24>c;c++){o[f+5*l]=(c+1)*(c+2)/2%64;var h=(2*f+3*l)%5,f=l%5,l=h}for(f=0;5>f;f++)for(l=0;5>l;l++)u[f+5*l]=l+5*((2*f+3*l)%5);f=1;for(l=0;24>l;l++){for(var p=h=c=0;7>p;p++){if(f&1){var d=(1<<p)-1;32>d?h^=1<<d:c^=1<<d-32}f=f&128?f<<1^113:f<<1}a[l]=s.create(c,h)}for(var v=[],f=0;25>f;f++)v[f]=s.create();n=n.SHA3=i.extend({cfg:i.cfg.extend({outputLength:512}),_doReset:function(){for(var e=this._state=[],t=0;25>t;t++)e[t]=new s.init;this.blockSize=(1600-2*this.cfg.outputLength)/32},_doProcessBlock:function(e,t){for(var n=this._state,r=this.blockSize/2,i=0;i<r;i++){var s=e[t+2*i],f=e[t+2*i+1],s=(s<<8|s>>>24)&16711935|(s<<24|s>>>8)&4278255360,f=(f<<8|f>>>24)&16711935|(f<<24|f>>>8)&4278255360,l=n[i];l.high^=f,l.low^=s}for(r=0;24>r;r++){for(i=0;5>i;i++){for(var c=s=0,h=0;5>h;h++)l=n[i+5*h],s^=l.high,c^=l.low;l=v[i],l.high=s,l.low=c}for(i=0;5>i;i++){l=v[(i+4)%5],s=v[(i+1)%5],f=s.high,h=s.low,s=l.high^(f<<1|h>>>31),c=l.low^(h<<1|f>>>31);for(h=0;5>h;h++)l=n[i+5*h],l.high^=s,l.low^=c}for(f=1;25>f;f++)l=n[f],i=l.high,l=l.low,h=o[f],32>h?(s=i<<h|l>>>32-h,c=l<<h|i>>>32-h):(s=l<<h-32|i>>>64-h,c=i<<h-32|l>>>64-h),l=v[u[f]],l.high=s,l.low=c;l=v[0],i=n[0],l.high=i.high,l.low=i.low;for(i=0;5>i;i++)for(h=0;5>h;h++)f=i+5*h,l=n[f],s=v[f],f=v[(i+1)%5+5*h],c=v[(i+2)%5+5*h],l.high=s.high^~f.high&c.high,l.low=s.low^~f.low&c.low;l=n[0],i=a[r],l.high^=i.high,l.low^=i.low}},_doFinalize:function(){var t=this._data,n=t.words,i=8*t.sigBytes,s=32*this.blockSize;n[i>>>5]|=1<<24-i%32,n[(e.ceil((i+1)/s)*s>>>5)-1]|=128,t.sigBytes=4*n.length,this._process();for(var t=this._state,n=this.cfg.outputLength/8,i=n/8,s=[],o=0;o<i;o++){var u=t[o],a=u.high,u=u.low,a=(a<<8|a>>>24)&16711935|(a<<24|a>>>8)&4278255360,u=(u<<8|u>>>24)&16711935|(u<<24|u>>>8)&4278255360;s.push(u),s.push(a)}return new r.init(s,n)},clone:function(){for(var e=i.clone.call(this),t=e._state=this._state.slice(0),n=0;25>n;n++)t[n]=t[n].clone();return e}}),t.SHA3=i._createHelper(n),t.HmacSHA3=i._createHmacHelper(n)}(Math);

define("libs/cryptojs/sha3", function(){});

/*global define */
define("DS/W3DPassport/dsp/utils/fingerprint", [
	"libs/cryptojs/md5", 
	"libs/cryptojs/sha256", 
	"libs/cryptojs/sha512",
	"libs/cryptojs/sha3"
],
		function(md5, sha256, sha512, sha3) {
			"use strict";
			var cryptos = {
				md5 : CryptoJS.MD5,
				sha256 : CryptoJS.SHA256,
				sha512 : CryptoJS.SHA512,
				sha3 : CryptoJS.SHA3
			}
			var getScreenDescription = function() {
				return "#" + screen.height + "x" + screen.width + "#";
			}

			var getBrowserDescription = function() {
				return "#" + navigator.userAgent + "@" + navigator.cpuClass
						+ "@" + navigator.platform + "#";
			}

			var getFonts = function() {
				return "#" + "#";
			}

			var getPlugins = function() {
				return "#" + "#";
			}

			return function(params) {
				var myDescription = getScreenDescription(),
				seed = params.seed || 1;
				myDescription += getBrowserDescription();
				myDescription += getFonts();
				myDescription += getPlugins();
				var hasher = null;
				if(params.hasher) {
					hasher = cryptos[params.hasher];
				}
				if(!hasher) {
					hasher = CryptoJS.SHA3;
				}
                                try {
                                    localStorage.fingerprint=myDescription;
                                } catch (error) {
                                    console.log("localStorage unavailable");
                                }
				return hasher(myDescription + seed).toString();
			};
		});

/*global define */
define("DS/W3DPassport/dsp/utils/addValidation", [
    "DS/W3DPassport/dsp/UWA",
    "DS/W3DPassport/dsp/utils/contains",
    "DS/W3DPassport/dsp/i18n/i18n"
], function(UWA, contains, i18n) {
    "use strict";

    var attributes = {
            DISABLED: "disabled",
            REQUIRED: "required",
            MIN_LENGTH: "minLength",
            MAX_LENGTH: "maxLength",
            PATTERN: "pattern"
        },
        novalidate = [
            "submit",
            "cancel",
            "button"
        ];

    function isCheckable(fieldElt) {
        var checkable = true,
            type;

        if ("FIELDSET" === fieldElt.nodeName) {
            checkable = false;
        } else if ("BUTTON" === fieldElt.nodeName) {
            checkable = false;
        } else if ("INPUT" === fieldElt.nodeName) {
            type = fieldElt.type || fieldElt.getAttribute("type");
            if (contains(novalidate, type)) {
                checkable = false;
            }
        }
        return checkable;
    }

    function checkValidity(fieldElt) {
        return fieldElt.checkValidity();
    }

    function checkRequired(fieldElt) {
        var validity = true;

        if (fieldElt.hasAttribute(attributes.REQUIRED) && !fieldElt.hasAttribute(attributes.DISABLED)) {
            validity = 0 < fieldElt.value.length;
        }
        return validity;
    }

      function checkCheckboxChecked(fieldElt) {
            var validity = true;

            if (fieldElt.type === "checkbox" && fieldElt.hasAttribute(attributes.REQUIRED) && !fieldElt.hasAttribute(attributes.DISABLED)) {
                validity = fieldElt.checked;
            }
            return validity;
        }

    function checkMinLength(fieldElt) {
        var minLength,
            validity = true;

        if (fieldElt.hasAttribute(attributes.MIN_LENGTH)) {
            minLength = parseInt(fieldElt.getAttribute(attributes.MIN_LENGTH), 10);
            if (!(isNaN(minLength))) {
                validity = minLength < fieldElt.value.length;
            }
        }
        return validity;
    }

    function checkMaxLength(fieldElt) {
        var maxLength,
            validity = true;

        if (fieldElt.hasAttribute(attributes.MAX_LENGTH)) {
            maxLength = parseInt(fieldElt.getAttribute(attributes.MAX_LENGTH), 10);
            if (!(isNaN(maxLength))) {
                validity = maxLength > fieldElt.value.length;
            }
        }
        return validity;
    }

    function checkPattern(fieldElt) {
        var pattern,
            validity = true;

        if (fieldElt.hasAttribute(attributes.PATTERN)) {
            pattern = new RegExp("^(" + fieldElt.getAttribute(attributes.PATTERN) + ")$");
            validity = pattern.test(fieldElt.value);
        }
        return validity;
    }

    function checkConfirm(fieldElt) {
        var original,
            confirm = fieldElt.getAttribute("name"),
            validity = true;

        if (confirm && confirm.test("_confirm")) {
            original = document.querySelector("[name=" +
                confirm.replace("_confirm", "")
                    .replace("[", "\\[")
                    .replace("]", "\\]")
                    .replace(".", "\\.") + "]");
            if (original) {
                validity = fieldElt.value === original.value;
            }
        }
        return validity;
    }

    // for fields such as username or email
    function checkFieldValidation(fieldElt) {
        var validity = true;

        if (fieldElt.classList.contains('field-validation-alert')) {
            validity = false;
        }
        return validity;
    }

  

    function validateField(fieldElt) {
        var checks = {},
            validity = true;

        // if ("function" === typeof fieldElt.checkValidity) {
        //     checks["checkValidity"] = checkValidity;
        // }
        checks["checkRequired"]=checkRequired;
        checks["checkMaxLength"]=checkMaxLength;
        checks["checkPattern"]=checkPattern;
        checks["checkFieldValidation"]=checkFieldValidation;
        checks["checkConfirm"]=checkConfirm;
        checks["checkMinLength"]=checkMinLength;
        checks["checkCheckboxChecked"]=checkCheckboxChecked;


        if (fieldElt.getAttribute("disabled") !== null) { // a disabled field must not be validated
            return true;
        }

        for (var key in checks) {
            if (!checks.hasOwnProperty(key)) { // ES5, don't use the key inherited from parent
                continue;
            }
            if (!validity) { // if one validator fails, stop checking the other
                break; // cannot break
            }
            let checkValidity = checks[key](fieldElt);
            if (!checkValidity) {
                UWA.Element.set.call(fieldElt, "data-invalid-reason", "");
                let attributeDataInvalidReason = fieldElt.getAttribute("data-invalid-reason");
                let reasons = attributeDataInvalidReason === null || attributeDataInvalidReason === "" ? [] : attributeDataInvalidReason.split(" ");
                reasons.push(key)
                var uniq = reasons.reduce(function (a, b) {
                    if (a.indexOf(b) < 0) a.push(b);
                    return a;
                }, []);
                UWA.Element.set.call(fieldElt, "data-invalid-reason", uniq.join(" "));
            } else {
                UWA.Element.set.call(fieldElt, "data-invalid-reason", "");
            }
            validity = validity && checkValidity;
        }
        if (validity) {
            UWA.Element.removeClassName.call(fieldElt, "invalid");
            UWA.Element.addClassName.call(fieldElt, "valid");
        } else {
            UWA.Element.removeClassName.call(fieldElt, "valid");
            UWA.Element.addClassName.call(fieldElt, "invalid");
        }
        return validity;
    }

    function handleFormSubmit(event) {
        let formElt = UWA.Event.findElement(event, "form");
        if (!formElt) {
            return;
        }
        let fields = formElt.elements, focused = false;
        [].forEach.call(fields, function(field) {
            if (isCheckable(field)) {
                let validity = validateField(field);
                _handleField(field); // toggle explanation message
                if (!validity) {
                    if (!focused){ // focus only on first invalid input
                        field.focus();
                        focused = true;
                    }
                    UWA.Event.stop(event);
                }
            }
        });
    }

    function handleFieldOnInput(event) {
        var fieldElt = UWA.Event.findElement(event, ".uwa-input");

        if (fieldElt && isCheckable(fieldElt)) {
            validateField(fieldElt);
        }
    }

    function handleFieldOnChange(event) {
        let field = UWA.Event.findElement(event, ".uwa-input");
        if (field && field.value === "") {
            // don't check anything if user hasn't filled anything, this avoids showing a red message when the user isn't interested by filling out the form
            // it also avoids a layout shift which can break some internjs ODT (notably when trying to click a link located on a page including a form)
            return;
        }
        _handleField(field);
    }

    function _handleField(field){
        if (field && isCheckable(field)) {
            // remove all message
            [].forEach.call(field.parentElement.getElementsByClassName("invalidField"), function(f) {
                f.remove();
            });
            // validate and add message if invalid
            if (!validateField(field)) {
                let attributeDataInvalidReason = field.getAttribute("data-invalid-reason");
                let reasons = attributeDataInvalidReason === null ? [] : attributeDataInvalidReason.split(" ");
                [].forEach.call(reasons, function(reason) {
                    var infoMessage = _buildValidationErrorMessage(reason);
                    if(infoMessage) {
                        infoMessage.inject(field.parentElement);
                    }
                });
            }
        }
    }

    function _buildValidationErrorMessage(reason) {
        switch (reason) {
            case "badInput":
            case "customError":
            case "patternMismatch":
            case "rangeOverflow":
            case "rangeUnderflow":
            case "stepMismatch":
            case "tooLong":
            case "tooShort":
            case "typeMismatch":
            case "valid":
            case "valueMissing":
            case "checkValidity":
            case "checkRequired":
            case "checkMinLength":
            case "checkMaxLength":
            case "checkPattern":
            case "checkConfirm":
                var infoMessage = UWA.createElement("div", {
                    "class": reason + " invalidField",
                    text: i18n.translate("invalid.field."+reason),
                    attributes: {
                        "data-dsp-i18n": "invalid.field."+reason
                    }
                });
                break;
            default:
                return null;
        }
        return infoMessage;

    }

    function parseValidationError(validityField) {
        switch (validityField) {
            case "badInput":
                //For example, if you have a number input element whose content is a string.
                var infoMessage = UWA.createElement("span", {
                    "class": "badInput invalidField",
                    text: i18n.translate("invalid.field.badInput")
                });

                break;
            case "customError":
                // setted with setCustomValidity() method.
                var infoMessage = UWA.createElement("span", {
                    "class": "customError invalidField",
                    text: i18n.translate("invalid.field.customError")
                });
                break;
            case "patternMismatch":
                //pattern error
                var infoMessage = UWA.createElement("span", {
                    "class": "patternMismatch invalidField",
                    text: i18n.translate("invalid.field.patternMismatch")
                });

                break;
            case "rangeOverflow":
                //does not conform to the constraints set by the element's max attribute.
                var infoMessage = UWA.createElement("span", {
                    "class": "rangeOverflow invalidField",
                    text: i18n.translate("invalid.field.rangeOverflow")
                });

                break;
            case "rangeUnderflow":
                //does not conform to the constraints set by the element's min attribute.
                var infoMessage = UWA.createElement("span", {
                    "class": "rangeUnderflow invalidField",
                    text: i18n.translate("invalid.field.rangeUnderflow")
                });
                break;
            case "stepMismatch":
                var infoMessage = UWA.createElement("span", {
                    "class": "stepMismatch invalidField",
                    text: i18n.translate("invalid.field.stepMismatch")
                });
                break;
            case "tooLong":
                var infoMessage = UWA.createElement("span", {
                    "class": "tooLong invalidField",
                    text: i18n.translate("invalid.field.tooLong")
                });
                break;
            case "tooShort":
                var infoMessage = UWA.createElement("span", {
                    "class": "tooShort invalidField",
                    text: i18n.translate("invalid.field.tooShort")
                });
                break;
            case "typeMismatch":
                var infoMessage = UWA.createElement("span", {
                    "class": "typeMismatch invalidField",
                    text: i18n.translate("invalid.field.typeMismatch")
                });
                break;
            case "valid":
                var infoMessage = UWA.createElement("span", {
                    "class": "valid invalidField",
                    text: i18n.translate("invalid.field.valid")
                });
                break;
            case "valueMissing":
                var infoMessage = UWA.createElement("span", {
                    "class": "valueMissing invalidField",
                    text: i18n.translate("invalid.field.valueMissing"),
                    attributes: {
                        "data-dsp-i18n": "invalid.field.valueMissing"
                    }
                });
                break;
            default:
                var infoMessage = UWA.createElement("span", {
                    "class": "valueMissing default",
                    text: i18n.translate("invalid.field.default")
                });
        }
        return infoMessage;

    }

    return function(formElt) {
        if ("FORM" === formElt.nodeName) {
            formElt.setAttribute("novalidate", "novalidate");
            UWA.Element.addEvent.call(formElt, "submit", handleFormSubmit);
            UWA.Element.addEvent.call(formElt, "input", handleFieldOnInput);
            UWA.Element.addEvent.call(formElt, "change", handleFieldOnChange);
        }
    };
});

function extractRequestParam() {
    var requestParams = {};
    window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
        function (_, key, value) {
            return requestParams[key] = value;
        });
    return requestParams;
}

/*global define */
define('DS/W3DPassport/dsp/form/createLoginForm',[
    "DS/W3DPassport/dsp/UWA",
    "DS/W3DPassport/dsp/utils/areRequiredOptionsMissing",
    "DS/W3DPassport/dsp/utils/addCaptchaToFields",
    "DS/W3DPassport/dsp/utils/fingerprint",
    "DS/W3DPassport/dsp/utils/addValidation"
], function (UWA, areRequiredOptionsMissing, addCaptchaToFields, fingerprint, addValidation) {
	function getCookie(name) {
		var start, end;
		if (document.cookie.length > 0) {
			start = document.cookie.indexOf(name + "=");
			if (start != -1) {
				start = start + name.length + 1;
				end = document.cookie.indexOf(";", start);
				if (end == -1) {
					end = document.cookie.length;
				}
				return unescape(document.cookie.substring(start, end));
			}
		}
		return "";
	}
	function isMultiSite(value) {
		return value === "true";
	}

    return function (options) {
        var config,
            formElt,
            uwaLoginForm,
            fields,
            myUsername,
            mySiteId;

        config = {
            displayCaptcha: false
        };
        UWA.extend(config, options);
        config.isMultiSite = "@@is_multi_site@@";
        var requestParams = extractRequestParam();

        const samlUserEmail = requestParams['pairing_user_email']

        myUsername = samlUserEmail || config.username || "";

        mySiteId = "";
        if(isMultiSite(config.isMultiSite)) {
			try {
	        	mySiteId = localStorage.getItem("x3ds_siteid") || "";
	        	if(!mySiteId) {
	        		if(location.search){
		        		var query = UWA.Utils.parseQuery(location.search);
		        		if(query.x3ds_siteid) {
		        			mySiteId = query.x3ds_siteid;
		        		} else if(query.service) {
		        			query = UWA.Utils.parseQuery(UWA.Utils.parseUrl(query.service).query);
		        			if(query.x3ds_siteid) {
		            			mySiteId = query.x3ds_siteid;
		            		}
		        		} else if(query.redirect) {
		        			query = UWA.Utils.parseQuery(UWA.Utils.parseUrl(atob(query.redirect)).query);
		        			if(query.x3ds_siteid) {
		            			mySiteId = query.x3ds_siteid;
		            		}
		        		}
		        	}
	        	}
			} catch(err){

			}
        }
        if (areRequiredOptionsMissing(config, [
                "url",
                "loginTicket" ])) {
            throw new Error("createLoginForm() required options are missing.");
        }
        fields = [{
            className: "hidden",
            type: "hidden",
            value: config.loginTicket,
            attributes: {
                name: "lt"
            }
        },{
            className: "hidden",
            type: "hidden",
            value: fingerprint({webgl: true, seed: config.seed}),
            attributes: {
                name: "fp"
            }
        }, {
            className: "username",
            type: "text",
            label: "Email or username",
            value: myUsername,
            attributes:{
                name: "username",
                id: "username",
                "data-dsp-i18n": "field.username_or_email.label",
                tabindex: 0,
                autofocus: "autofocus",
                // placeholder: isMultiSite(config.isMultiSite) ? "Username" : "Email or username",
                required: "required",
                // cannot use disabled attribute here since the value does not gets submitted at form Submit
                readonly: config.oidcLoginUiEnabled ? (config.displayPassword ? true : false) : false,
                autocomplete: "username"
            },
        }];

        if (config.displayPassword) {
            fields.push({
                className: "password",
                type: "password",
                label: "Password",
                attributes: {
                    id: "password",
                    name: "password",
                    "data-dsp-i18n": "field.password.label",
                    tabindex: 0,
                    // placeholder: "Password",
                    autofocus: "autofocus",
                    required: "required",
                    autocomplete: "password"
                }
            });
        }

        if(isMultiSite(config.isMultiSite)) {
			var siteIdDescription = {
                type: "text",
                label: "Site ID",
                value: mySiteId,
                attributes:{
                    name: "siteid",
                    tabindex: 0,
                    placeholder: "Site ID"
                }
            };
			if(!location.pathname.contains("/admin-tools/login")) {
				siteIdDescription.attributes.required = "required";
			}
			fields.push(siteIdDescription);
        }
        if (config.displayCaptcha) {
            config.needsCaptcha = true;
            addCaptchaToFields(config, fields);
        }

        if (config.displayPassword) {
            fields.push({
                className: "remember-me aligned-checkbox",
                type: "checkbox",
                label: "Remember me",
                attributes: {
                    name: "rememberMe",
                    "data-dsp-i18n": "field.rememberme.label",
                    tabindex: 0
                }
            });
        }
        var commandsToCreate = [{
            type: "submit",
            value: config.displayPassword ? "Log in" : "Continue",
            attributes: {
                "data-dsp-i18n": config.displayPassword ? "commons.action.logIn" : "commons.action.logIn.continue",
                tabindex: 0
            }
        }];
        if(config.displayPassword && config.oidcLoginUiEnabled){
            commandsToCreate.push({
                type: "",
                value: "Back",
                attributes: {
                    "data-dsp-i18n": "field.back.button.label",
                    tabindex: 0
                },
                events: {
                    onClick: function (e) {
                        window.location.href = config.previousPassportUrl;
                    }
                }
            })
        }
        uwaLoginForm = new UWA.dsp.component.InputForm({
            className: "uwa-input-form login-form",
            // legend: {
            //     "value": "3DEXPERIENCE ID",
            //     "attributes": {
            //         "data-dsp-i18n": (config.footnoteNls !== undefined
            //             && config.footnoteNls !== ""
            //             && config.footnoteNls !== null) ?
            //             "commons.panel.logIn.title.with.footnote" :
            //             "commons.panel.logIn.title",
            //         "class": "login-legend"
            //     }
            // },
            fields: fields,
            commands: commandsToCreate
        });
        
        // checking to see if displayPassword is true then the username Input field must be grayed out
        if(config.displayPassword && config.oidcLoginUiEnabled){
            var usernameInput = uwaLoginForm.elements.container.querySelector('.field.username input[name="username"]');
            usernameInput.setStyle('background-color','#f1f1f1');
        }
        
        var userAgent = navigator.userAgent;
        var checkBoxDiv  = uwaLoginForm.elements.container.querySelector('.uwa-checkbox');
        var checkBoxInput =  uwaLoginForm.elements.container.querySelector('.uwa-checkbox-input');
        if (checkBoxInput != null
            && checkBoxDiv != null
            && userAgent != null
            && userAgent.includes("3DSNativeApp/")
        ) {
            checkBoxDiv.classList.add('checked');
            checkBoxInput.checked = true;
        }

        if (!config.authorizeRememberMe) {
            uwaLoginForm.elements.container.getElement(".field.remember-me>label").remove();
        }
        formElt = uwaLoginForm.getContent();
        formElt.removeEvent("submit");
        formElt.action = config.url;
        formElt.method = "POST";
        addValidation(formElt);
        if(isMultiSite(config.isMultiSite)) {
			var fields = formElt.elements,
				usernameField = fields["username"],
				siteIdField = fields["siteid"];
			usernameField.addEventListener("blur",function(event){
				var loginValue = usernameField.value,
				idxOfAt = loginValue.lastIndexOf('@'),
				idxPoint = loginValue.indexOf('.', idxOfAt),
				myUsername,
				mySiteId;
				if(idxOfAt > 0 && idxPoint < 0) {
					// If we have a site id
					usernameField.value = loginValue.substring(0, idxOfAt);
					if(siteIdField) {
						siteIdField.value = loginValue.substring(idxOfAt + 1);
					}
				}
			},true);
        	UWA.Element.addEvent.call(formElt, "submit", function(event) {
        		if(usernameField && siteIdField) {
                	var username = usernameField.value,
                	siteId = siteIdField.value;
                	if(username && siteId) {

                		var idxOfAt = username.lastIndexOf('@'),
                		idxPoint = username.indexOf('.', idxOfAt);
                    	if(idxOfAt > 0 && idxPoint < 0) {
                    		usernameField.value = username;
                    	} else {
                    		usernameField.value = username + "@" + siteId;
                    	}

                	}
                }
        	});
        }
        return uwaLoginForm;
    };
});

/*global define */
define("DS/W3DPassport/dsp/utils/decodeHtml", [], function () {
    "use strict";
    return function decodeHtml(html) {
        var text = document.createElement("textarea");
        text.innerHTML = html;
        return text.value;
    };
});

/*global define */
define("DS/W3DPassport/dsp/form/createUserForm", [
    "DS/W3DPassport/dsp/UWA",
    "DS/W3DPassport/dsp/utils/areRequiredOptionsMissing",
    "DS/W3DPassport/dsp/utils/addCaptchaToFields",
    "DS/W3DPassport/dsp/utils/addCsrfToFields",
    "DS/W3DPassport/dsp/utils/addValidation",
    "DS/W3DPassport/dsp/utils/decodeHtml"
], function (UWA,
             areRequiredOptionsMissing,
             addCaptchaToFields,
             addCsrfToFields,
             addValidation,
             decodeHtml) {
    "use strict";

    return function (options) {
        var config,
            fields,
            commands,
            userForm,
            formElt;

        // default values
        config = {
            values: {},
            showPasswordFields: false,
            ajax: false
        };
        UWA.extend(config, options);
        if (areRequiredOptionsMissing(config, [
            "url",
            "apiUrlCountries"])) {
            throw new Error("createUserForm() required options are missing");
        }
        fields = config.fields;
        // Unescape HTML
        for (var i = 0; i < fields.length; i++) {
            if (fields[i].value) {
                fields[i].value = decodeHtml(fields[i].value);
            }
        }
        addCsrfToFields(config, fields);

        if (config.actionI18n == "commons.action.register" || config.actionI18n == "commons.action.update") {
            if (config.captchaRegister !== undefined
                && config.captchaRegister === "true") {

                config.needsCaptcha = true;
                addCaptchaToFields(config, fields);
            }
        }
        if (true == config.updateAllowed || true == config.createAllowed) {
            commands = [{
                type: "submit",
                value: "Submit",
                attributes: {
                    "data-dsp-i18n": config.actionI18n
                }
            }];
        } else {
            commands = [];
        }
        if (config.cancelUrl) {
            commands.push({
                type: "cancel",
                value: "Cancel",
                attributes: {
                    "data-dsp-i18n": "commons.action.cancel"
                },
                events: {
                    onClick: function (e) {
                        window.location.href = config.cancelUrl;
                    }
                }
            });
        }
        userForm = new UWA.dsp.component.InputForm({
            fields: fields,
            className: config.className,
            commands: commands,
            events: {
                onSubmit: function (event, values) {
                    UWA.Data.request(config.url, {
                        method: 'POST',
                        data: values
                    });
                }
            }
        });
        formElt = userForm.getContent();
        if (!config.ajax) {
            formElt.removeEvent("submit");
            formElt.action = config.url;
            formElt.method = "POST";
        }
        addValidation(formElt);

        return userForm;
    };
});

/*global define */
define("DS/W3DPassport/dsp/form/createRegisterForm", [
    "DS/W3DPassport/dsp/UWA",
    "DS/W3DPassport/dsp/form/createUserForm"
], function (UWA, createUserForm) {
    "use strict";

    var createRegisterForm = function (options) {
        var config;

        config = {
            className: "uwa-input-form register-form",
            actionI18n: "commons.action.register",
            showPasswordFields: true,
            ajax: false
        };
        UWA.extend(config, options);
        return createUserForm(config);
    };

    return createRegisterForm;
});

/*global define, document, window */
define("DS/W3DPassport/dsp/utils/loginTicketTimeoutMsg", [
    "DS/W3DPassport/dsp/UWA"
], function (UWA) {
    "use strict";

    var hiddenProp,
        visibilityChangeProp,
        intervalID,
        intervalDuration = 119000, /* 1m59s, 1sec for latancy */
        loginTicketUrl;
        

    function updateLoginTicket() {
        var ltInput = UWA.Element.getElement.call(document.body, "[name=lt]");

        UWA.Data.getJson(loginTicketUrl, function (r) {
            ltInput.value = r['lt'];
        });
    }

    function handleVisibilityChange() {
        if (document[hiddenProp]) {
            window.clearInterval(intervalID);
        } else {
            updateLoginTicket();
            intervalID = window.setInterval(updateLoginTicket, intervalDuration);
        }
    }

    return function (ltURL) {
    	loginTicketUrl = ltURL;
        if (typeof document.hidden !== "undefined") {
            hiddenProp = "hidden";
            visibilityChangeProp = "visibilitychange";
        } else if (typeof document.mozHidden !== "undefined") {
            hiddenProp = "mozHidden";
            visibilityChangeProp = "mozvisibilitychange";
        } else if (typeof document.msHidden !== "undefined") {
            hiddenProp = "msHidden";
            visibilityChangeProp = "msvisibilitychange";
        } else if (typeof document.webkitHidden !== "undefined") {
            hiddenProp = "webkitHidden";
            visibilityChangeProp = "webkitvisibilitychange";
        }
        /* Support addEventListener and visibility API */
        if (("function" === typeof document.addEventListener) && hiddenProp) {
            intervalID = window.setInterval(updateLoginTicket, intervalDuration);
            document.addEventListener(visibilityChangeProp, handleVisibilityChange, false);
        } else {
            window.setTimeout(function () {
                var loginForm = UWA.Element.getElement.call(document, ".login-form"),
                    commands = loginForm.getElement(".commands"),
                    ltMsgs = UWA.Element.getElement.call(document, ".lt-expired-error"),
                    len;

                len = loginForm.elements.length;
                while (0 < len) {
                    len -= 1;
                    loginForm.elements[len].disabled = true;
                }
                commands.hide();
                ltMsgs.removeClassName("hidden");
                window.setTimeout(function () { ltMsgs.addClassName("active"); }, 0);
            }, 120000);
        }
    };
});

/*global define, document */
define("DS/W3DPassport/dsp/utils/regExpUtil", [
	
], function() {
    "use strict";

    var regExpUtil = {};
    
    regExpUtil.escapeSpecialChars = function (str) {
    	if (str) {
    		return str.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
    	}
    	return str;
    };
    
    return regExpUtil;
});


/*global define */
define("DS/W3DPassport/dsp/utils/createPasswordInformationPanel", [
    "DS/W3DPassport/dsp/i18n/i18n",
    "DS/W3DPassport/dsp/utils/regExpUtil"
], function (i18n, regExpUtil) {

    var mPasswordInputName,
            mUser;
    function getInputForm(name) {
        return UWA.Element.getElements.call(document, "[name=data\\[" + name + "\\]]")[0];
    }
    function getInputFormValue(name) {
        if (mUser && mPasswordInputName !== name) {
            return mUser.fields[name];
        } else {
            return UWA.Element.getElements.call(document, "[name=data\\[" + name + "\\]]")[0].value || '';
        }
    }
    function setRuleStatus(ruleName, ok) {
        var rule = UWA.Element.getElements.call(document, ".rule[name=" + ruleName.replace(/\./g, '\\.') + "]")[0];
        if (rule) {
            if (ok) {
                rule.removeClassName("ko");
                rule.addClassName("ok");
            } else {
                rule.addClassName("ko");
                rule.removeClassName("ok");
            }
        }
    }
    function containsIgnoreCase(haystack, needle) {
        return haystack.match(new RegExp(regExpUtil.escapeSpecialChars(needle), "gi"));
    }
    function refreshPasswordInformation(pwdPolicy) {
        var pwdInput = UWA.Element.getElements.call(document, "[name=data\\[" + mPasswordInputName + "\\]]")[0],
                pwdValue = pwdInput.value,
                pwdLength = pwdValue ? pwdValue.length : 0,
                selectedLevel = 'bad',
                allOk = true,
                nbSpecialChars = pwdPolicy.specialCharactersAllowed.length;

        if (!pwdPolicy.allowPasswordWithUsername) {
            var username = getInputFormValue("username");
            if (pwdValue && username && username != '' && containsIgnoreCase(pwdValue, username)) {
                allOk = false;
                setRuleStatus("rule.pwd.allow.username", false);
            } else {
                setRuleStatus("rule.pwd.allow.username", true);
            }
        }
        if (!pwdPolicy.allowPasswordWithFirstname) {
            var firstName = getInputFormValue("firstName");
            if (pwdValue && firstName && firstName != '' && containsIgnoreCase(pwdValue, firstName)) {
                allOk = false;
                setRuleStatus("rule.pwd.allow.firstName", false);
            } else {
                setRuleStatus("rule.pwd.allow.firstName", true);
            }
        }
        if (!pwdPolicy.allowPasswordWithLastname) {
            var lastName = getInputFormValue("lastName");
            if (pwdValue && lastName && lastName != '' && containsIgnoreCase(pwdValue, lastName)) {
                allOk = false;
                setRuleStatus("rule.pwd.allow.lastName", false);
            } else {
                setRuleStatus("rule.pwd.allow.lastName", true);
            }
        }
        if (pwdPolicy.minimumLength > 0) {
            if (pwdValue.length >= pwdPolicy.minimumLength) {
                setRuleStatus("rule.pwd.minLength", true);
            } else {
                setRuleStatus("rule.pwd.minLength", false);
                allOk = false;
            }
        }
        var nbDigits = pwdValue.replace(/[^0-9]/g, '').length,
                nbLower = pwdValue.replace(/[^a-z]/g, '').length,
                nbUpper = pwdValue.replace(/[^A-Z]/g, '').length,
                nbSpecials = 0,
                nbUnauthorized = 0,
                iterPwd;
        for (iterPwd = 0; iterPwd < pwdLength; iterPwd++) {
            var currChar = pwdValue[iterPwd];
            if (('0' <= currChar && currChar <= '9') ||
                    ('a' <= currChar && currChar <= 'z') ||
                    ('A' <= currChar && currChar <= 'Z')) {
                continue;
            } else if (pwdPolicy.specialCharactersAllowed.indexOf(currChar) >= 0) {
                nbSpecials++;
            } else {
                nbUnauthorized++;
            }
        }
        if (pwdPolicy.minimumDigits > 0) {
            if (nbDigits >= pwdPolicy.minimumDigits) {
                setRuleStatus("rule.pwd.minDigits", true);
            } else {
                setRuleStatus("rule.pwd.minDigits", false);
                allOk = false;
            }
        }
        if (pwdPolicy.minimumLetters > 0) {
            if (nbLower + nbUpper >= pwdPolicy.minimumLetters) {
                setRuleStatus("rule.pwd.minLetters", true);
            } else {
                setRuleStatus("rule.pwd.minLetters", false);
                allOk = false;
            }
        }
        if (pwdPolicy.minimumLowerCase > 0) {
            if (nbLower >= pwdPolicy.minimumLowerCase) {
                setRuleStatus("rule.pwd.minLowerCase", true);
            } else {
                setRuleStatus("rule.pwd.minLowerCase", false);
                allOk = false;
            }
        }
        if (pwdPolicy.minimumUpperCase > 0) {
            if (nbUpper >= pwdPolicy.minimumUpperCase) {
                setRuleStatus("rule.pwd.minUpperCase", true);
            } else {
                setRuleStatus("rule.pwd.minUpperCase", false);
                allOk = false;
            }
        }
        if (pwdPolicy.minimumSpecial > 0) {
            if (nbSpecials >= pwdPolicy.minimumSpecial) {
                setRuleStatus("rule.pwd.minSpecial", true);
            } else {
                setRuleStatus("rule.pwd.minSpecial", false);
                allOk = false;
            }
        }
        if (pwdPolicy.specialCharactersAllowed.length > 0) {
            if (nbUnauthorized > 0) {
                setRuleStatus("rule.pwd.special.allowed", false);
                allOk = false;
            } else {
                setRuleStatus("rule.pwd.special.allowed", true);
            }
        }

        if (allOk) {
            var h = pwdValue.length;
            if (h < 8) {
                selectedLevel = 'low';
            } else if (h <= 8) {
                selectedLevel = 'medium';
            } else {
                selectedLevel = 'high';
            }
            UWA.Element.removeClassName.call(pwdInput, "invalid");
            UWA.Element.removeClassName.call(pwdInput, "invalid-password");
            UWA.Element.addClassName.call(pwdInput, "valid");
        } else {
            selectedLevel = 'bad';
            UWA.Element.removeClassName.call(pwdInput, "valid");
            //addValidation does not detect password as invalid since checkValidity returns true, so we need a post check
            UWA.Element.addClassName.call(pwdInput, "invalid");
            UWA.Element.addClassName.call(pwdInput,  "invalid-password");
        }

        UWA.Element.getElements.call(document, ".passwordInfoStrength div").forEach(function (elt) {
            elt.removeClassName('selected')
        });
        UWA.Element.getElements.call(document, ".passwordInfoStrength ." + selectedLevel).forEach(function (elt) {
            elt.addClassName('selected')
        });
        return allOk;
    }


    return function (pwdInputField, form, config) {
        mPasswordInputName = pwdInputField;
        mUser = config.user;
        var passwordEntries = UWA.Element.getElements.call(document, "[name=data\\[" + mPasswordInputName + "\\]]"),
                pwdPolicy = config.passwordPolicy;
        if (passwordEntries && passwordEntries.length > 0) {
            // For each password entry (even if there should be just one)
            // we add a description panel + a strength indicator
            passwordEntries.forEach(function (passwordField) {
                var passwordStrengthMeter,
                    passwordInfos;

                passwordStrengthMeter = new UWA.Element("div", {
                    "class": "passwordInfoStrength",
                    html: [
                        new UWA.Element("div", {
                            "class": "bad"
                        }),
                        new UWA.Element("div", {
                            "class": "low"
                        }),
                        new UWA.Element("div", {
                            "class": "medium"
                        }),
                        new UWA.Element("div", {
                            "class": "high"
                        })
                    ]
                });
                passwordInfos = new UWA.Element("div", {
                    "class": "passwordInfo",
                    html: [{
                            tag: "div",
                            "class": "passwordInfoRules",
                            html: (function () {
                                var rules = [],
                                        createRule = function (label, args) {
                                            var description = {
                                                "class": "description",
                                                "data-dsp-i18n": label,
                                                text: label
                                            }
                                            if (args && args.length > 0) {
                                                description['data-dsp-i18n-param'] = args;
                                            }
                                            return new UWA.Element("div", {
                                                "class": "rule",
                                                "name": label,
                                                html:
                                                        UWA.Element("div", description)

                                            });
                                        };
                                if (!pwdPolicy.allowPasswordWithUsername) {
                                    rules.push(createRule("rule.pwd.allow.username"));
                                }
                                if (!pwdPolicy.allowPasswordWithFirstname) {
                                    rules.push(createRule("rule.pwd.allow.firstName"));
                                }
                                if (!pwdPolicy.allowPasswordWithLastname) {
                                    rules.push(createRule("rule.pwd.allow.lastName"));
                                }
								if(pwdPolicy.minimumLength > 0) {
									rules.push(
                                        createRule("rule.pwd.minLength", [pwdPolicy.minimumLength]));
								}
								if(pwdPolicy.minimumDigits > 0) {
									rules.push(
                                        createRule("rule.pwd.minDigits", [pwdPolicy.minimumDigits]));
								}
								if(pwdPolicy.minimumLetters > 0) {
									rules.push(
                                        createRule("rule.pwd.minLetters", [pwdPolicy.minimumLetters]));
								}
								if(pwdPolicy.minimumLowerCase > 0) {
									rules.push(
                                        createRule("rule.pwd.minLowerCase", [pwdPolicy.minimumLowerCase]));
								}
								if(pwdPolicy.minimumUpperCase > 0) {
									rules.push(
                                        createRule("rule.pwd.minUpperCase", [pwdPolicy.minimumUpperCase]));
								}
                                if (pwdPolicy.minimumSpecial > 0) {
                                    rules.push(createRule("rule.pwd.minSpecial", [pwdPolicy.minimumSpecial]));
                                }
                                if (pwdPolicy.specialCharactersAllowed && pwdPolicy.specialCharactersAllowed.length > 0) {
                                    rules.push(createRule("rule.pwd.special.allowed",
                                            [
                                                pwdPolicy.specialCharactersAllowed
												.map(function(elt) {
													if(elt === ',') {
														return "#######COMMA#######";
													} else {
														return elt;
													}
												})
                                                .join(' ')
                                            ]
                                            ));
                                }
                                return rules;
                            })()
                        }]
                });
                passwordStrengthMeter.inject(passwordField.getParent(".field"), "bottom");
                passwordInfos.inject(passwordField.getParent(".field"), "bottom");
                passwordInfos.addClassName("invisible");
                UWA.Element.addEvent.call(passwordField, 'focus', function (event) {
                    refreshPasswordInformation(pwdPolicy);
                    passwordInfos.addClassName('visible');
                    passwordInfos.removeClassName('invisible');
                });
                UWA.Element.addEvent.call(passwordInfos, 'click', function (event) {
                    passwordInfos.addClassName("invisible");
                    passwordInfos.removeClassName('visible');
                });
                UWA.Element.addEvent.call(passwordField, "input", function () {
                    passwordInfos.addClassName('visible');
                    passwordInfos.removeClassName('invisible');
                });
                UWA.Element.addEvent.call(passwordField, "focusout", function () {
                    passwordInfos.addClassName('invisible');
                    passwordInfos.removeClassName('visible');
                });
                UWA.Element.addEvent.call(passwordField, 'keyup', function (event) {
                    refreshPasswordInformation(pwdPolicy);
                });
               


                if (!mUser) {
                    ['username', 'firstName', 'lastName'].forEach(function (fieldName) {
                        var fieldInvolved = getInputForm(fieldName);
                        UWA.Element.addEvent.call(fieldInvolved, 'keyup', function (event) {
                            refreshPasswordInformation(pwdPolicy);
                        });
                    });
                }
                UWA.Element.addEvent.call(form, "submit", function (event) {
                    var allOk = refreshPasswordInformation(pwdPolicy);
                    if (!allOk) {
                        UWA.Event.stop(event);                       
                    }
                });               
            });
        }
    };
});


define("DS/W3DPassport/dsp/utils/hightlightBackendError", [
	
], function () {
    "use strict";

    function hightlightFieldName(form, fieldNames) {
        var field,
            i;

        if ((Array.isArray(fieldNames)) && (form !== undefined)
                && (typeof form.getElement === "function")) {
            i = fieldNames.length;
            while (i > 0) {
                i -= 1;
                field = form.getElement('[name="data\[' + fieldNames[i] + '\]"]');
                if (field) {
                    field.addClassName("invalid");
                }
            }
        }
    }

    /**
     * Check in error messages send form the backend if a field is concerned by
     * an error and give it a css class to be highlighted.
     *
     * @param {UWA.Element} form The form element
     * @param {Object} option configuration object sent by the backend in json
     * @returns {undefined}
     */
    return function(form, option) {
        var errorMsgs = option.errorMsgs,
            i;

        if ((errorMsgs !== undefined) && (errorMsgs.length !== undefined) &&
                (errorMsgs.length > 0)) {
            i = errorMsgs.length;
            while (i > 0) {
                i-= 1;
                hightlightFieldName(form, errorMsgs[i].affectedFieldNames);
            }
        }
    };
});

/*global define,Object */
define("DS/W3DPassport/dsp/utils/isIE11", [

], function () {
    "use strict";
    /**
     * true if IE11
     */
    return function () {
        return !!window.MSInputMethodContext && !!document.documentMode;
    };
});

/*global define */
/**
 * Track a user visit to a page
 * @param {String} pageURL     - The url of the currently visited page.
 * @param {String} [pageTitle] - The title of the currently visited page.
 * see more param on TrackerAPI.js
 */
define("DS/W3DPassport/dsp/utils/createTrackerPageView", [
    "DS/W3DPassport/dsp/utils/isIE11"
], function (isIE11) {
    "use strict";

    return function (config, pageURL, pageTitle, persDimension, callback) {

        // IE11 compatibility
        if (isIE11()) {
            config.trackerJs = false;
            config.gdprEnabled = false;
        }
        /*
        TrackerAPI.trackPageView({
            *          pageURL: window.location.href,
            *          pageTitle: 'My 3DEXPERIENCE Page',
            *          pageLanguage: 'en',
            *          appID: "SDdfs45",
            *          tenant: "dsfs75",
            *          persDim: {
            *              pd1: 'faq',
            *              pd2: 'true',
            *              pd3: 'search'
            *          },
            *          persVal: {
            *              pv1: 45
            *          }
            *      });
            * */
        if (config.trackerJs !== undefined
            && config.trackerJs === "true"
            && window.location === window.parent.location) {

            require(["DS/Usage/TrackerAPI"], function (TrackerAPI) {
                TrackerAPI.trackPageView({
                    pageURL: pageURL,
                    pageTitle: pageTitle,
                    /* pageLanguage: 'en',
                     appID: "SDdfs45",
                     tenant: "dsfs75",*/
                    persDim: persDimension,
                    /*  persVal: {
                          pv1: 45
                      }*/
                    callback: callback
                });
            });
        }
    };
});

/*global define */
/*Description:

* Track a user event of a page
* @param {String} eventCategor - Typically the object that was interacted. (e.g. 'Video')
* @param {String} eventAction - The type of interaction (e.g. 'play')
See more param on TrackerAPI.js
*/
define("DS/W3DPassport/dsp/utils/createTrackerPageEvent", [
    "DS/W3DPassport/dsp/utils/isIE11"

], function (isIE11) {
    "use strict";

    return function (config, eventCategory, eventAction, persDimension, callback) {

        // Tracking to detect an event
        /* * Track an event of the current page
         * @method trackPageEvent
         * @memberof module:DS/Usage/TrackerAPI#
         * @param {Object} options - Page event tracked information
         * @param {String} options.eventCategory - Typically the object that was interacted. (e.g. 'Video')
         * @param {String} options.eventAction - The type of interaction (e.g. 'play')
         * @param {String} [options.eventLabel] - Useful for categorizing events (e.g. 'Fall Campaign')
         * @param {Integer} [options.eventValue] - Value differs from the other components in that it is an integer rather than string, so use it to assign a numerical value to a tracked event. For example, you could use it to provide the time in seconds for an player to load, or you might trigger a dollar value when a specific playback marker is reached on a video player.
         * @param {String} [options.appID] - Store the application ID information
         * @param {String} [options.tenant] - Store the user tenant information
         * @param {Object<String,String>} [options.persDim] - Let you store your own dimensions eg {pd1: 'price', pd2:'time', [...], pd20: 'unit'}.
         * @param {Object<String,Double>} [options.persVal] - Let you store your own values eg {pv1: 45, pv2: 10, [...], pv20: 65}.
         * @example
         * */

        // IE11 compatibility
        if (isIE11()) {
            config.trackerJs = false;
            config.gdprEnabled = false;
        }

        if (config.trackerJs !== undefined
            && config.trackerJs === "true"
            && window.location === window.parent.location) {

            require(["DS/Usage/TrackerAPI"], function (TrackerAPI) {
                TrackerAPI.trackPageEvent({
                    eventCategory: eventCategory,
                    eventAction: eventAction,
                    /* eventLabel: '',
                     eventValue: '',
                     appID: '',
                     tenant: '',*/
                    persDim: persDimension,
                    //persVal: '',
                    callback: callback
                });
            });
        }
    };
});

/*global define,localStorage */
define("DS/W3DPassport/login/rememberLastUser", [
    "UWA/Element"
], function (Element) {
    "use strict";
    window.passport_multisite = "@@is_multi_site@@";

	function setCookie(name, value, days) {
		var expires;
		if (days) {
			var date = new Date();
			date.setDate(date.getDate() + days);
			expires = "; expires=" + date.toGMTString();
		} else {
			expires = "";
		}
		document.cookie = name + "=" + value + expires + "; path=/";
	}

	function isSiteId(login) {
        var indexOfAtChar = login.lastIndexOf('@');
        var indexOfPointChar = login.indexOf('.', indexOfAtChar); // check if is domain or not
        return indexOfAtChar > 0 && indexOfPointChar < 0;
    }

    function decomposeSiteId(login) {
        var indexOfAtChar = login.lastIndexOf('@');
        return {
            "username": login.substr(0, indexOfAtChar),
            "siteId": login.substr(indexOfAtChar + 1)
        }
    }

    return {
        load: function (form, userNameWelcome, iAmNotLink, welcomeMsg) {
            var username;
            var loginField = Element.getElement.call(form, '[name="username"]');
            var loginLegend = Element.getElement.call(form, '[class="login-legend"]');

            if (localStorage) {
                username = localStorage.getItem("lastUser");
                if (username && loginField) {
                    loginField.value = username;
                    loginField.getClosest(".field").addClassName("hidden");
                    userNameWelcome.textContent = username;
                    iAmNotLink.removeClassName("hidden");
                    iAmNotLink.getElement(".username").setText(username);
                    welcomeMsg.removeClassName("hidden");
                    // loginLegend.addClassName("hidden");

                    if (window.passport_multisite === "true") {
                        var siteIdField = Element.getElement.call(form, '[name="siteid"]');
                        siteIdField.value = localStorage.getItem("x3ds_siteid");
                    }
                }
            }
        },
        save: function (form) {
            var loginField = Element.getElement.call(form, "[name=\"username\"]");
            if (localStorage) {
            	var loginValue = loginField.value;
            	var siteIdValue = "";
                try {
                    if (window.passport_multisite === "true" && isSiteId(loginValue)) {
                        var decomposed = decomposeSiteId(loginValue);
                        loginValue = decomposed.username;
                        siteIdValue = decomposed.siteId;
                    }
                    localStorage.setItem("lastUser", loginValue);
                    localStorage.setItem("x3ds_siteid", siteIdValue);
                } catch (error) {
                    console.log("localStorage unavailable");
                }
            }
        }
    };
});

define("DS/WebappsUtils/Map",[],function(){"use strict";function e(e,t){for(var n=0;n<e.size;++n)if(e._content[n].key===t)return n;return-1}var t=function(){Object.defineProperty(this,"_content",{value:[]}),Object.defineProperty(this,"size",{get:function(){return this._content.length}})};return t.prototype.get=function(e){return(this._content.filter(function(t){return t.key===e})[0]||{}).value},t.prototype.set=function(t,n){var r=e(this,t);return r>-1?this._content[r].value=n:this._content.push({key:t,value:n}),this},t.prototype.delete=function(t){var n=e(this,t);return-1!==n&&(this._content.splice(n,1),!0)},t.prototype.has=function(t){return e(this,t)>-1},t.prototype.clear=function(){this._content.splice(0,this._content.length)},t.prototype.forEach=function(e,t){this._content.forEach(function(n){e.call(t,n.value,n.key,this)},this)},window.Map||t}),function(e){"use strict";define("DS/WebappsUtils/Promise",function(){function t(e){return null==e?e+"":typeof e}var n,r=e.Promise,o=r&&"resolve"in r&&"reject"in r&&"all"in r&&"race"in r&&function(){var e;return new r(function(t){e=t}),"function"==typeof e}(),i="pending",a="sealed",s="fulfilled",c="rejected",u=void 0!==e.setImmediate?e.setImmediate:e.setTimeout,f=[];function l(){for(var e=0;e<f.length;e++)f[e][0](f[e][1]);f=[],n=!1}function p(e,t){f.push([e,t]),n||(n=!0,u(l,0))}function h(e){var t=e._then;e._then=void 0;for(var n=0;n<t.length;n++)b(t[n])}function d(e){e._state=s,h(e)}function m(e){e._state=c,h(e)}function g(e,t){e._state===i&&(e._state=a,e._data=t,p(d,e))}function y(e,t){e!==t&&w(e,t)||g(e,t)}function v(e,t){e._state===i&&(e._state=a,e._data=t,p(m,e))}function w(e,n){var r;try{if(e===n)throw new TypeError("A promises callback cannot return that same promise.");if(n&&("function"===t(n)||"object"===t(n))){var o=n.then;if("function"===t(o))return o.call(n,function(t){r||(r=!0,n!==t?y(e,t):g(e,t))},function(t){r||(r=!0,v(e,t))}),!0}}catch(t){return r||v(e,t),!0}return!1}function b(e){var n=e.owner,r=n._state,o=n._data,i=e[r],a=e.then;if("function"===t(i)){r=s;try{o=i(o)}catch(e){v(a,e)}}w(a,o)||(r===s&&y(a,o),r===c&&v(a,o))}function x(e){if("function"!==t(e))throw new TypeError("Promise constructor takes a function argument");if(this instanceof x==="false")throw new TypeError("Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function.");this._then=[],function(e,t){function n(e){v(t,e)}try{e(function(e){y(t,e)},n)}catch(e){n(e)}}(e,this)}return x.prototype={constructor:x,_state:i,_then:null,_data:void 0,then:function(e,t){var n={owner:this,then:new this.constructor(function(){}),fulfilled:e,rejected:t};return this._state===s||this._state===c?p(b,n):this._then.push(n),n.then},catch:function(e){return this.then(null,e)}},x.all=function(e){if(!Array.isArray(e))throw new TypeError("You must pass an array to Promise.all().");return new this(function(n,r){var o=[],i=0;function a(e){return i++,function(t){o[e]=t,--i||n(o)}}for(var s,c=0;c<e.length;c++)(s=e[c])&&"function"===t(s.then)?s.then(a(c),r):o[c]=s;i||n(o)})},x.race=function(e){if(!Array.isArray(e))throw new TypeError("You must pass an array to Promise.race().");return new this(function(n,r){for(var o,i=0;i<e.length;i++)(o=e[i])&&"function"===t(o.then)?o.then(n,r):n(o)})},x.resolve=function(e){return e&&"object"===t(e)&&e.constructor===this?e:new this(function(t){t(e)})},x.reject=function(e){return new this(function(t,n){n(e)})},o?r:x})}(this),define("DS/WebappsUtils/Utils",function(){"use strict";function e(e){return"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g,function(t){var n,r=16*(e?(n=1e4*Math.sin(e++))-Math.floor(n):Math.random())|0;return("x"===t?r:3&r|8).toString(16)})}var t,n,r,o,i,a,s,c={uuid:e,getUUID:e,navigationType:(s="unknown",window.performance.navigation&&(s=0===(a=window.performance.navigation.type)?"navigate":1===a?"reload":2===a?"back":"unknown"),s),getChecksum:function(){var e=arguments,t=String(e[0]),n=e[1],r=0,o=t.length;for(n=n||305419896;r<o;r++)n+=t.charCodeAt(r)*r;return Math.abs(parseInt(n,10)).toString(36)},beaconPolyfill:function(e,t){var n=window.XMLHttpRequest?new XMLHttpRequest:new window.ActiveXObject("Msxml2.XMLHTTP");n.open("POST",e,!1),n.setRequestHeader("Content-Type","text/plain;charset=UTF-8"),n.setRequestHeader("Accept","*/*"),n.send(t)},clientEngine:(i=navigator.userAgent?navigator.userAgent.toLowerCase():"","ie"===(t=i.match(/(opera|ie|firefox|chrome|version)[\s\/:]([\w\d\.]+)?.*?(safari|version[\s\/:]([\w\d\.]+)|$)/)||i.match(/(webkit)[\s\/:]([\w\d\.]+)/)||i.match(/(trident)\/.*rv:([\d\.]+)/))[1]||"trident"===t[1]?(o=(n=window.document&&document.documentMode)&&n.toString(),r="ie"):(o="opera"===t[1]&&t[4]?t[4]:t[2],r="version"===t[1]?t[3]:t[1]),{name:r,version:o,userAgent:i}),clientPlatform:function(){var e=navigator&&navigator.userAgent.toLowerCase()||"",t=(navigator&&navigator.platform.toLowerCase()||"").split(" ")[0],n=/ip(?:ad|od|hone)/.test(e)?"ios":/webos|wossystem/.test(e)?"webos":/blackberry/.test(e)?"blackberry":/android/.test(e)?"android":/mac|win|linux/.test(t)?t:"other";return{name:n,mobile:"ios"===n||"android"===n||"blackberry"===n||!!e.match(/tablet/),resheight:screen&&screen.height||0,reswidth:screen&&screen.width||0}}(),isVirtualKeyboardOpen:function(){var e=function(){try{return top.document.documentElement}catch(e){return document.documentElement}}(),t={},n=!1,r=e.clientHeight,o=window.screen.height;function i(){if(!c.clientPlatform.mobile||"ios"===c.clientPlatform.name)return!1;var o=a(),i=t[o];if(t[o]=Math.max(t[o]||0,e.clientHeight),!i||e.clientHeight===r)return n;r=e.clientHeight}function a(){return window.screen.width+"x"+window.screen.height}return setTimeout(i),window.addEventListener("resize",function(){r===e.clientHeight?setTimeout(function e(){if(window.screen.height===o)return setTimeout(e,250);i(),o=window.screen.height},250):i()}),function(){return n=e.clientHeight<t[a()]}}(),copy:function(e){var t,n,r,o=e.contentEditable,i=e.readOnly;return"ios"===this.clientPlatform.name?(e.contentEditable=!0,e.readOnly=!0,(t=document.createRange()).selectNodeContents(e),(n=window.getSelection()).removeAllRanges(),n.addRange(t),e.setSelectionRange(0,e.value.length),e.contentEditable=o,e.readOnly=i):e.select(),r=document.execCommand("copy"),e.blur(),r},getCookie:function(e){var t,n,r=[];try{r=document.cookie.split("; ")}catch(e){}return(t=r.filter(function(t){return t.split("=")[0]===e})).length&&(n=t[0].split("=")[1]),n},setPersistantCookie:function(e,t,n){var r,o=new Date;o.setFullYear(o.getFullYear()+1),r=e+"="+t+"; expires="+o.toUTCString(),n&&(r+="; domain="+n),r+="; path=/",document.cookie=r},setCookie:function(e,t,n,r){var o=new Date(Date.now()+6e4*r),i=e+"="+t;n&&(i+="; domain="+n),i+="; path=/",r&&(i+="; expires="+o.toUTCString()),document.cookie=i},removeCookie:function(e,t){var n;n=e+"=undef; expires="+new Date(0).toUTCString(),t&&(n+="; domain="+t),n+="; path=/",document.cookie=n},debounce:function(e,t,n){var r;return function(){var o,i=this,a=arguments;o=n&&!r,clearTimeout(r),r=setTimeout(function(){r=null,n||e.apply(i,a)},t),o&&e.apply(i,a)}},throttle:function(e,t,n,r,o){var i,a,s,c=null,u=0,f=function(){u=new Date,c=null,s=e.apply(i,a)};return function(){var l=new Date;u||n||(u=l);var p=t-(l-u);return i=o||this,a=arguments,p<=0?(clearTimeout(c),c=null,u=l,s=e.apply(i,a)):!c&&r&&(c=setTimeout(f,p)),s}},sha256:function e(t){function n(e,t){return e>>>t|e<<32-t}for(var r,o,i=Math.pow,a=i(2,32),s="",c=[],u=8*t.length,f=e.h=e.h||[],l=e.k=e.k||[],p=l.length,h={},d=2;p<64;d++)if(!h[d]){for(r=0;r<313;r+=d)h[r]=d;f[p]=i(d,.5)*a|0,l[p++]=i(d,1/3)*a|0}for(t+="";t.length%64-56;)t+="\0";for(r=0;r<t.length;r++){if((o=t.charCodeAt(r))>>8)return;c[r>>2]|=o<<(3-r)%4*8}for(c[c.length]=u/a|0,c[c.length]=u,o=0;o<c.length;){var m=c.slice(o,o+=16),g=f;for(f=f.slice(0,8),r=0;r<64;r++){var y=m[r-15],v=m[r-2],w=f[0],b=f[4],x=f[7]+(n(b,6)^n(b,11)^n(b,25))+(b&f[5]^~b&f[6])+l[r]+(m[r]=r<16?m[r]:m[r-16]+(n(y,7)^n(y,18)^y>>>3)+m[r-7]+(n(v,17)^n(v,19)^v>>>10)|0);(f=[x+((n(w,2)^n(w,13)^n(w,22))+(w&f[1]^w&f[2]^f[1]&f[2]))|0].concat(f))[4]=f[4]+x|0}for(r=0;r<8;r++)f[r]=f[r]+g[r]|0}for(r=0;r<8;r++)for(o=3;o+1;o--){var k=f[r]>>8*o&255;s+=(k<16?0:"")+k.toString(16)}return s}};return c}),
/*! Copyright 2014 Dassault Systèmes */
function(e){"use strict";var t,n,r=null,o=null,i=null,a=function(t){return r=t+("/"===t[t.length-1]?"":"/"),e.dsDefaultWebappsBaseUrl=r,require.config({baseUrl:r,paths:{DS:".",vendors:"vendors",UWA:"UWA2/js"}}),r};t=(i="object"==typeof location?location.pathname:"/").substr(0,i.lastIndexOf("/")+1)+"../",a("string"==typeof e.dsDefaultWebappsBaseUrl?e.dsDefaultWebappsBaseUrl:t),"string"==typeof e.dsProxifiedWebappsBaseUrl&&(n=e.dsProxifiedWebappsBaseUrl,o=n+("/"===n[n.length-1]?"":"/")),define("DS/WebappsUtils/WebappsUtils",function(){return{getWebappsBaseUrl:function(){return r},getProxifiedWebappsBaseUrl:function(){return o},_setWebappsBaseUrl:a,getWebappsAssetUrl:function(e,t){try{var n=window.dsVersion||parent.window.dsVersion,o=window.dsPrefix||parent.window.dsPrefix;return r+e+"/assets/"+t+(n&&-1!==t.indexOf(".")&&-1===r.indexOf(o+"resources/"+n+"/")?(-1===t.indexOf("?")?"?v=":"&v=")+n:"")}catch(n){return r+e+"/assets/"+t}}}}),define("DS/WebappsUtils",["DS/WebappsUtils/WebappsUtils"],function(e){return e}),define("WebappsUtils/WebappsUtils",["DS/WebappsUtils/WebappsUtils"],function(e){return e})}(this),define("DS/WebappsUtils/Inherit",function(){"use strict";function e(e){return null==e?e+"":typeof e}function t(t){return!("object"!==e(t)||t.nodeType||null!=t&&t===t.window)&&!(t.constructor&&!n(t.constructor.prototype,"isPrototypeOf"))}function n(e,t){return null!=e&&hasOwnProperty.call(e,t)}function r(e){return function(){var r,i,a,s,c,u,f=arguments[0]||{},l=1,p=arguments.length,h=!1;for("boolean"==typeof f&&(h=f,f=arguments[l]||{},l++),"object"!=typeof f&&"function"!=typeof f&&(f={});l<p;l++)if(null!=(r=arguments[l]))for(i in r)if(e||n(r,i)){if(a=f[i],f===(s=r[i]))continue;h&&s&&(t(s)||(c=Array.isArray(s)))?(c?(c=!1,u=a&&Array.isArray(a)?a:[]):u=a&&t(a)?a:{},f[i]=o.extend(h,u,s)):void 0!==s&&(f[i]=s)}return f}}var o={assign:r(),extend:r(!0),clone:function(){var t=arguments[0],n=!1;return"boolean"===e(t)&&(n=t,t=arguments[1]),o.extend(n,{},t)},inherit:function(){var e,t=arguments[0],n=arguments[1];return t.prototype=Object.create("function"==typeof n?n.prototype:n),t.prototype.constructor=t,arguments.length>2&&((e=[].slice.call(arguments,2)).unshift(t.prototype),o.assign.apply(null,e)),t}};return o}),
/*! Copyright 2015 Dassault Systèmes */
define("DS/WebappsUtils/Console",function(){"use strict";var e=Array.prototype.slice,t=Function.prototype.apply,n=function(n,r){return function(){"undefined"!=typeof console&&console[n]?t.call(console[n],console,e.call(arguments)):r&&r.apply(r,e.call(arguments))}},r={};return r.log=n("log"),r.info=n("info",r.log),r.debug=n("debug",r.log),r.warn=n("warn",r.log),r.error=n("error",r.log),r.trace=n("trace",function(){r.log((new Error).stack)}),r}),define("DS/WebappsUtils/Emitter",function(){"use strict";return{on:function(e,t){return this.listeners=this.listeners||{},e in this.listeners?this.listeners[e].push(t):this.listeners[e]=[t],this},trigger:function(e){var t=Array.prototype.slice;return this.listeners=this.listeners||{},e in this.listeners&&this.invoke(this.listeners[e],t.call(arguments,1)),"*"in this.listeners&&this.invoke(this.listeners["*"],arguments),this},invoke:function(e,t){for(var n=0,r=e.length;n<r;n++)e[n].apply(this,t)},off:function(e,t){var n;return e in this.listeners&&(t?(n=this.listeners[e].indexOf(t))>=0&&this.listeners[e].splice(n,1):this.listeners[e]=[]),this}}}),function(e){"use strict";define("DS/WebappsUtils/Performance",function(){var t={},n=e.performance||{},r=[],o=+new Date,i=function(e){for(var t=e.charAt(0).toUpperCase()+e.slice(1),r=[e,"webkit"+t,"ms"+t,"moz"+t],o=0;o<r.length;o++)if(n[r[o]])return n[r[o]].bind(n);return null},a=["navigationStart","unloadEventStart","unloadEventEnd","redirectStart","redirectEnd","fetchStart","domainLookupStart","domainLookupEnd","connectStart","connectEnd","secureConnectionStart","requestStart","responseStart","responseEnd","domLoading","domInteractive","domContentLoadedEventStart","domContentLoadedEventEnd","domComplete","loadEventStart","loadEventEnd"];return t.navigation=n.navigation||{},t.timing=n.timing||{},t.now=i("now")||function(){return+new Date-o},t.mark=i("mark")||function(e){if(!e)throw new TypeError("Failed to execute 'mark' on 'Performance': 1 argument required, but only 0 present.");if(-1!==a.indexOf(e))throw new DOMException("Failed to execute 'mark' on 'Performance': "+e+" is part of the PerformanceTiming interface, and cannot be used as a mark name.");r.push({name:e,entryType:"mark",startTime:t.now(),duration:0})},t.clearMarks=i("clearMarks")||function(e){r=r.filter(function(t){return"mark"!==t.entryType||void 0!==e&&t.name!==e})},t.measure=i("measure")||function(n,i,s){var c,u,f=0,l=0;if(!n)throw new TypeError("Failed to execute 'mark' on 'Performance': 1 argument required, but only 0 present.");if(!t.timing.navigationStart&&-1!==a.indexOf(i)||-1!==a.indexOf(s))e.console.warn("Your browser does not support the navigation timing API.");else{if(i){if(0===(c=r.filter(function(e){return"mark"===e.entryType&&e.name===i})).length)throw new DOMException("Failed to execute 'measure' on 'Performance': The mark "+i+" does not exist.");l=c.sort(function(e,t){return e.startTime<t.startTime})[0].startTime}else l=t.timing.navigationStart||o;if(s){if(0===(u=r.filter(function(e){return"mark"===e.entryType&&e.name===s})).length)throw new DOMException("Failed to execute 'measure' on 'Performance': The mark "+s+" does not exist.");f=u.sort(function(e,t){return e.startTime<t.startTime})[0].startTime}else f=t.now();r.push({name:n,entryType:"measure",startTime:l,duration:f-l})}},t.clearMeasures=i("clearMeasures")||function(e){r=r.filter(function(t){return"measure"!==t.entryType||void 0!==e&&t.name!==e})},t.getEntries=i("getEntries")||function(){return r},t.getEntriesByType=i("getEntriesByType")||function(e){return r.filter(function(t){return t.entryType===e})},t.getEntriesByName=i("getEntriesByName")||function(e){return r.filter(function(t){return t.name===e})},(!e.performance||"function"!=typeof e.performance.mark&&"function"!=typeof e.performance.measure)&&(e.performance?Object.keys(t).forEach(function(n){e.performance[n]||(e.performance[n]=t[n])}):e.performance=t),t})}(this);
define("DS/W3DPassport/dsp/utils/createFieldHintInformationPanel", [
], function () {

    return function (inputFieldName, form, config) {
        var fieldHints = UWA.Element.getElements.call(document, "[name=data\\[" + inputFieldName + "\\]]")[0].parentElement.getElementsByClassName("hint");
        var field = UWA.Element.getElements.call(document, "[name=data\\[" + inputFieldName + "\\]]")[0];

        UWA.Element.getElements.call(document, "[name=data\\[" + inputFieldName + "\\]]")[0].parentElement.getElementsByClassName("hints")[0].addClassName('invisible');

        UWA.Element.addEvent.call(field, 'focusout', function (event) {
            field.parentElement.getElementsByClassName("hints")[0].addClassName('invisible');
            field.parentElement.getElementsByClassName("hints")[0].removeClassName('visible');
        });

        UWA.Element.addEvent.call(field, 'focus', function (event) {
            field.parentElement.getElementsByClassName("hints")[0].addClassName('visible');
            field.parentElement.getElementsByClassName("hints")[0].removeClassName('invisible');
           var  fieldValue = event.target.value;
           var bad = 0;
            for (var i = 0, l = fieldHints.length; i < l; i++) {               
                var fieldRegexExp = RegExp(fieldHints[i].getAttribute("pattern"));
                var result = fieldRegexExp.test(fieldValue);
                if (result) {    
                    fieldHints[i].removeClassName('ko');   
                    fieldHints[i].addClassName('ok'); 
                }
                else {                   
                    bad = bad + 1;
                    fieldHints[i].removeClassName('ok');  
                    fieldHints[i].addClassName('ko');                      
                }
            }
        });

        UWA.Element.addEvent.call(field, 'keyup', function (event) {
            field.parentElement.getElementsByClassName("hints")[0].addClassName('visible');
            field.parentElement.getElementsByClassName("hints")[0].removeClassName('invisible');
           var  fieldValue = event.target.value;
           var bad = 0;
            for (var i = 0, l = fieldHints.length; i < l; i++) {               
                var fieldRegexExp = RegExp(fieldHints[i].getAttribute("pattern"));
                var result = fieldRegexExp.test(fieldValue);
                if (result) {    
                    fieldHints[i].removeClassName('ko');   
                    fieldHints[i].addClassName('ok'); 
                }
                else {                   
                    bad = bad + 1;
                    fieldHints[i].removeClassName('ok');  
                    fieldHints[i].addClassName('ko');                      
                }
            }
        });
    }


});

/*global define */
define("DS/W3DPassport/dsp/utils/displayHidePassword", [
    "DS/W3DPassport/dsp/UWA"
], function (UWA) {
    "use strict";

    return function (passwordfield) {
        if (passwordfield != null) {
            var togglePassword = UWA.createElement("span", {"class": "toggle-password visible"});

            togglePassword.inject(passwordfield.parentElement);
            togglePassword.addEventListener("click", toggleClicked);

            function toggleClicked() {
                if (this.hasClassName("visible")) {
                    passwordfield.type = "text";
                } else {
                    passwordfield.type = "password";
                }
                togglePassword.classList.toggle("visible");

            }
        }
    };
});

function extractRequestParam() {
    var requestParams = {};
    window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
        function (_, key, value) {
            return requestParams[key] = value;
        });
    return requestParams;
}

/*global define */
define("DS/W3DPassport/dsp/utils/createModalPanelPairing", [
    "DS/W3DPassport/dsp/UWA"
], function (UWA) {
    "use strict";

    /* quick and dirty modal until we migrate to something better... do not use if possible*/
    return function (body, toggleLoginPanelCallback, toggleRegisterPanelCallback) {

        var modalRoot = UWA.createElement("div", {
            id: "dsp-modal-root",
            class: "dsp-modal-root modal modal-root",
            style: "display:block"
        });

        var modalWrap = UWA.createElement("div", {
            id: "dsp-modal-wrap",
            class: "dsp-modal-wrap modal-wrap"
        });

        var modalContent = UWA.createElement("div", {
            id: "dsp-modal-content",
            class: "dsp-modal-content modal-content",
        });

        var modalHeader = UWA.createElement("div", {
            id: "dsp-modal-header",
            class: "dsp-modal-header modal-header",
            html: {
                tag: "h4",
                "data-dsp-i18n": "commons.modal.user.account.association.title",
            }
        });

        modalHeader.inject(modalContent);

        var modalText = [
            {
                tag: "p",
                "data-dsp-i18n": "commons.modal.user.account.association.msg.1",
            }, {
                tag: "p",
                "data-dsp-i18n": "commons.modal.user.account.association.msg.2",
            }
        ]
        var requestParams = extractRequestParam();

        const samlUserEmail = requestParams['pairing_user_email']

        if (samlUserEmail) {
            modalText.push({
                    tag: "p",
                    html: [
                        {
                            tag: "span",
                            "data-dsp-i18n": "commons.modal.user.account.association.msg.3"
                        },
                        {
                            tag: "strong",
                            text: " " + samlUserEmail + ". "
                        },
                        {
                            tag: "span",
                            "data-dsp-i18n": "commons.modal.user.account.association.msg.4"
                        }
                    ]
                })
        }

        var modalBody = UWA.createElement("div", {
            id: "dsp-modal-body",
            class: "dsp-modal-body modal-body",
            html: modalText
        });
        modalBody.inject(modalContent);

        var modalFooter = UWA.createElement("div", {
            id: "dsp-modal-footer",
            class: "dsp-modal-footer modal-footer",
        });

        var buttonLogin = UWA.createElement("button", {
            id: "dsp-modal-button-login",
            class: "btn-primary btn btn-root",
            html: {
                "data-dsp-i18n": "commons.modal.user.account.association.btn.login",
            }
        });

        var buttonRegister = UWA.createElement("button", {
            id: "dsp-modal-button-register",
            class: "btn-default btn btn-root",
            html: {
                "data-dsp-i18n": "commons.modal.user.account.association.btn.register",
            }
        });

        buttonLogin.inject(modalFooter);
        buttonRegister.inject(modalFooter);
        modalFooter.inject(modalContent);


        modalContent.inject(modalWrap);
        modalWrap.inject(modalRoot);
        modalRoot.inject(body);

        /* blur background */
        var modalOverlay = UWA.createElement("div", {
            id: "dsp-modal-overlay",
            class: "dsp-modal-overlay modal-overlay in"
        });
        modalOverlay.inject(body);

        function hideModal() {
            if (!modalOverlay.hasClassName("hide")) {
                modalOverlay.addClassName("hide");
            }
            modalRoot.hide();
        }

        function clickBtnModal(e) {
            hideModal();
            if (e.currentTarget.id === "dsp-modal-button-register") {
                toggleRegisterPanelCallback();
            } else {
                toggleLoginPanelCallback();
            }
        }

        buttonLogin.addEventListener("click", clickBtnModal);
        buttonRegister.addEventListener("click", clickBtnModal);
        modalRoot.show();

    };
});

/*jslint browser:true */
/*global define, require */
/**
 * Entry point for the login view
 */
define("DS/W3DPassport/login", [
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
    "DS/W3DPassport/dsp/utils/addValidation",
    "DS/WebappsUtils/Utils",
    "DS/W3DPassport/dsp/utils/createFieldHintInformationPanel",
    "DS/W3DPassport/dsp/utils/displayHidePassword",
    "DS/W3DPassport/dsp/utils/createModalPanelPairing",
    "DS/W3DPassport/dsp/utils/isIE11"
], function (config,
             i18n,
             UWA,
             createLoginForm,
             createRegisterForm,
             addMessagesToPanel,
             loginTicketTimeoutMsg,
             createLanguagePicker,
             createPasswordInformationPanel,
             hightlightBackendError,
             createTrackerPageView,
             createTrackerPageEvent,
             rememberLastUser,
             addValidation,
             Utils,
             createFieldHintInformationPanel,
             displayHidePassword,
             createModalPanelPairing,
             isIE11) {
    "use strict";


    var panel,
        connectPanel,
        connectWithSAML,
        registerContainer,
        customBackgroundImg,
        loginForm,
        loginFormElt,
        registerForm,
        captcha,
        loginTicketExpiredMsg,
        links,
        needHelpLink,
        linkItems = [],
        registerFormLoaded = false,
        cookiesMsg,
        repo = {};

    i18n.init(config);

    // IE11 compatibility
    if (isIE11()) {
        config.trackerJs = false;
        config.gdprEnabled = false;
    }
    // Init Tracking
    if (config.trackerJs !== undefined
        && config.trackerJs === "true"
        && window.location === window.parent.location) {
        require(["DS/Usage/UsageManager"], function (UsageManager) {
            UsageManager.init('/upload', 'passport');
        });
    }

    function prepareRegisterForm(registerForm) {
        var fields = registerForm.getElements(".field"),
            field,
            index;

        index = fields.length;
        while (index-- > 0) {
            field = fields[index];
            if (!(field.hasClassName("required")) && !(field.hasClassName("display"))) {
                field.remove();
            }
        }
    }

    function onLoadRegisterForm(data) {
        registerForm = createRegisterForm(UWA.extend(config, {
            fields: data,
            url: config.registerUrl,
            apiUrlCountries: config.apiUrlCountries
        }));
        registerForm.getContent().addClassName("hidden");
        var registerFormElt = registerForm.getContent();

        //Tracking part
        if (config.trackerJs !== undefined
            && config.trackerJs === "true" && window.location === window.parent.location) {
            registerFormElt.addEvent("submit", function () {

                    //Hack needed to have synchrone call of tracker API
                    arguments[0].preventDefault();
                    //check that the form can be submitted ( no invalid field)
                    if (registerFormElt.getElementsByClassName("invalid").length == 0 && registerFormElt.getElementsByClassName("invalid-password").length == 0) {
                        var registerHashObject = {
                            pd1: 'regular',
                            pd2: Utils.sha256(document.querySelector("[name='data[email]']").value),
                            pd3: Utils.sha256(document.querySelector("[name='data[username]']").value)
                        };
                        // don't give back submit event to the form since there are invalid fields
                        var callbackFct = function () {
                            registerFormElt.submit();
                        };
                    } else {

                        var registerHashObject = {
                            pd1: 'regular',
                            pd2: Utils.sha256(document.querySelector("[name='data[email]']").value),
                            pd3: Utils.sha256(document.querySelector("[name='data[username]']").value)
                        };
                        //Generate error message for pd4:
                        for (var i = 0; i < registerFormElt.getElementsByClassName("invalid").length; i++) {
                            var invalidFieldClassList = registerFormElt.getElementsByClassName("invalid")[i].classList;
                            var invalidFieldName = registerFormElt.getElementsByClassName("invalid")[i].name;
                            if (invalidFieldName == "data[username]") {
                                if (invalidFieldClassList.value.contains("field-validation-alert")) {
                                    registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "username already exists in these directories: " + repo["username"];
                                } else {
                                    registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "invalid username requirements";
                                }
                            } else if (invalidFieldName == "data[email]") {
                                if (invalidFieldClassList.value.contains("field-validation-alert")) {
                                    registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "email already exists in these directories: " + repo["email"];
                                } else {
                                    registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "invalid email requirements";
                                }
                            } else if (invalidFieldName == "data[firstName]") {
                                registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "invalid firstName requirements";
                            } else if (invalidFieldName == "data[lastName]") {
                                registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "invalid lastName requirements";
                            } else if (invalidFieldName == "data[password_confirm]") {
                                registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "invalid confirm password";
                            } else if (invalidFieldName == "data[country]") {
                                registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "no country selected";
                            } else if (invalidFieldName == "data[dpp_consent]") {
                                //data privacy
                                registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "no Privacy Policy selected";
                            } else {
                                //system failure?
                                registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "system failure";
                            }
                        }
                        if (registerFormElt.getElementsByClassName("invalid-password").length > 0) {
                            registerHashObject.pd4 = (registerHashObject.pd4 ? registerHashObject.pd4 = registerHashObject.pd4 + ", " : registerHashObject.pd4 = "") + "invalid password requirements";
                        }
                    }

                    createTrackerPageEvent(config, "register", "registerTentative", registerHashObject, callbackFct);

                }
            );
        }
        // End of tracker part

        //Check Field Validation
        /*
        * Check the validity of a field with a name . Like field username or email
        * add focusout event for call to api when the focus is out
        * validate or unvalidate the field
        */
        function checkFieldValidation(fieldElmt) {
            var fieldName = fieldElmt.name.split(['['])[1].split([']'])[0];

            fieldElmt.addEventListener("click", function (event) {
                this.removeClassName("field-validation-alert");
            });
            //To check unicity
            fieldElmt.addEventListener("focusout", function (event) {
                var values = {};
                var value = event.target.value;
                
                
                // like {"email":"juanr@yahoo.com"}
                values[fieldName] = value;

                var configTracker = config;
                //Tracker for solidworks
                if (fieldName == "email") {
                    var loginHashObject = {
                        pd1: 'regular',
                        pd2: Utils.sha256(value),
                        pd3: ''
                    };
                } else {
                    var loginHashObject = {
                        pd1: 'regular',
                        pd2: '',
                        pd3: Utils.sha256(value)
                    };
                }


                // send call only if valid field
                if (this.classList.contains("valid") == true || (this.classList.contains("invalid") == true && this.classList.contains("field-validation-alert") == true)) {
                    validateField(event.target.value, fieldElmt, configTracker);
                } /*else if (this.classList.contains("invalid")) {
                    if (registerFormElt.getElementsByClassName(fieldName + "-field-validation-alert")[0]) {
                        registerFormElt.getElementsByClassName(fieldName + "-field-validation-alert")[0].remove();
                        fieldElmt.removeClassName("field-validation-alert");
                    }
                }*/
            });

        }

        function manageDataTransferPolicyDisplay(){
            registerFormElt.elements["data[datatransfer_consent]"].getParent().getParent().getParent().addClassName("invisible");
            registerFormElt.elements["data[datatransfer_consent]"].setAttribute("disabled","disabled");

            UWA.Element.addEvent.call(registerFormElt, "change",  function (event){
                if(event.target.name =="data[country]" || event.target.name =="data[dpp_consent]"){
                    var countrySelect = registerFormElt.elements["data[country]"].value;
                    var isDppConsentCheck = registerFormElt.elements["data[dpp_consent]"].checked;
                   for (var i = 0; i < config.dataTransferConsentCountries.length; i++) {
                      if(countrySelect == config.dataTransferConsentCountries[i] && isDppConsentCheck ){
                          registerFormElt.elements["data[datatransfer_consent]"].getParent().getParent().getParent().removeClassName("invisible");
                          registerFormElt.elements["data[datatransfer_consent]"].getParent().getParent().getParent().addClassName("visible");
                          registerFormElt.elements["data[datatransfer_consent]"].removeAttribute("disabled");
                          break;
                      } else{
                          registerFormElt.elements["data[datatransfer_consent]"].getParent().getParent().getParent().removeClassName("visible");
                          registerFormElt.elements["data[datatransfer_consent]"].getParent().getParent().getParent().addClassName("invisible");
                          registerFormElt.elements["data[datatransfer_consent]"].setAttribute("disabled","disabled");


                      }
                   }

                }
            });

      }

        function validateField(field, fieldElmt, configTracker) {
            var fieldName = fieldElmt.name.split(['['])[1].split([']'])[0];

            if (!field || field === '')
                return

            var values = {};
            values[fieldName] = field;

            UWA.Data.request(config.fieldValidationUrl, {
                method: 'POST',
                headers: {
                    "Accept": "application/json",
                    "Content-Type": "application/json"
                },
                data: JSON.stringify(values),
                onComplete: function (conf) {
                    var noError, config;
                    config = JSON.parse(conf);
                    noError = config.valid == "failed" === false;
                    if (noError) {
                        //No error get result
                        if (config.valid == false) {
                            // valid field
                            if (registerFormElt.getElementsByClassName(fieldName + "-field-validation-alert").length == 0) {
                                var infoMessage = UWA.createElement("span", {
                                    "class": fieldName + "-field-validation-alert invalidField",
                                    text: i18n.translate(config.errorMsg)
                                });
                                infoMessage.inject(fieldElmt.parentElement);

                                fieldElmt.addClassName("field-validation-alert");
                                fieldElmt.removeClassName("valid");
                                fieldElmt.addClassName("invalid");
                                repo[fieldName] = config.repo;
                                // If config.repo is present it means the check was for Email/Username unicity
                                if (fieldName == "email" && config.repo != null) {
                                    loginHashObject.pd4 = "Email already exists in these directories: " + config.repo;
                                    createTrackerPageEvent(configTracker, "register", "checkEmailFail", loginHashObject);
                                } else if (fieldName == "username" && config.repo != null) {
                                    loginHashObject.pd4 = "Username already exists in these directories: " + config.repo;
                                    createTrackerPageEvent(configTracker, "register", "checkUsernameFail", loginHashObject);
                                }
                            }
                        } else {
                            // remove potential warning span
                            if (registerFormElt.getElementsByClassName(fieldName + "-field-validation-alert").length > 0) {
                                registerFormElt.getElementsByClassName(fieldName + "-field-validation-alert")[0].remove();
                                fieldElmt.removeClassName("field-validation-alert");
                                fieldElmt.removeClassName("invalid");
                                fieldElmt.addClassName("valid");
                            }
                        }

                        addValidation(registerFormElt);
                    }
                },
                withCredentials: true
            });
        }

        registerFormElt.elements["data[username]"].classList.add("valid")
        registerFormElt.elements["data[email]"].classList.add("valid")

        checkFieldValidation(registerFormElt.elements["data[username]"]);
        checkFieldValidation(registerFormElt.elements["data[email]"]);

        validateField(registerFormElt.elements["data[username]"].value, registerFormElt.elements["data[username]"], config);
        validateField(registerFormElt.elements["data[email]"].value, registerFormElt.elements["data[email]"], config);

        if(registerFormElt.elements["data[datatransfer_consent]"] !== undefined){
            manageDataTransferPolicyDisplay();
        }



        prepareRegisterForm(registerForm.getContent());
        registerContainer.addContent(registerForm);
        registerContainer.addContent(UWA.createElement("div", {
            "class": "register-links hidden",
            html: [{
                tag: "a",
                href: "#login",
                html: [
                    {
                        tag: "span",
                        "data-dsp-i18n": "commons.already-have-an-account-question",
                        text: "Already have an account?"
                    },{
                        tag: "span",
                        text: " "
                    },{
                        tag: "span",
                        "data-dsp-i18n": "commons.action.logIn",
                        text: "Log in"
                    },
                ],
                events: {
                    click: function (event) {
                        // return to login page so tracking of page view
                        UWA.Event.stop(event);
                        toggleLoginPanel();
                    }
                }
            }]
        }));
        createPasswordInformationPanel("password", registerForm.getContent(), config);
        displayHidePassword(registerFormElt.elements["data[password]"]);
        displayHidePassword(registerFormElt.elements["data[password_confirm]"]);
        createFieldHintInformationPanel("username");

        if (config.captchaRegister !== undefined
            && config.captchaRegister === "true") {

            if (config.captchaType === "legacyCaptcha") {
                captcha = new UWA.dsp.component.CaptchaDisplay({
                    captchaImgUrl: config.captchaImgUrl
                });

                captcha.inject(registerForm.getContent().getElement(".field.captcha"), "before");
            } else {
                require(["recaptcha"], function (recaptcha) {
                    captcha = UWA.createElement('div', {
                        "class": "g-recaptcha",
                        "data-sitekey": config.captchaClientId
                    });
                    captcha.inject(registerForm.getContent().querySelector(".uwa-submit"), "before");
                });
            }
        }
        hightlightBackendError(registerForm.getContent(), config);

    }



    if (config.trackerJs !== undefined
        && config.trackerJs === "true" && window.location === window.parent.location) {
        // Tracking to detect login page view
        createTrackerPageView(config, "LogIn", "LoginPage");

        // Tracking to detect failure
        if (config.errorMsgs.length > 0) {
            var loginHashObject = {};
            //Filter if it is a login with username or email.
            // pd1 must be the type of connexion (regular, 2FA...)
            // pd2 must be the hash corresponding to email connexion
            // pd3 must be the hash corresponding to username connexion
            if (localStorage.lastUser) {
                if ((localStorage.lastUser).includes('@')) {
                    var loginHashObject = {
                        pd1: 'regular',
                        pd2: Utils.sha256(localStorage.lastUser)
                    };
                } else {
                    var loginHashObject = {
                        pd1: 'regular',
                        pd2: '',
                        pd3: Utils.sha256(localStorage.lastUser)
                    };
                }
            }
            //Filter for CAPTCHA error  and register error
            for (var i = 0; i < config.errorMsgs.length; i++) {
                if (config.errorMsgs[i].code == "error.captcha.wrong") {
                    loginHashObject.pd4 = "Wrong captcha";
                    createTrackerPageEvent(config, "captcha", "captchaFailure", loginHashObject);
                    createTrackerPageEvent(config, "LogIn", "logInFailure", loginHashObject);
                } else if (config.errorMsgs[i].code == "error.authentication.lockout") {
                    loginHashObject.pd4 = "Account locked out";
                    createTrackerPageEvent(config, "LogIn", "logInFailure", loginHashObject);
                } else if (config.errorMsgs[i].code == "error.password.expired") {
                    loginHashObject.pd4 = "Password Expired";
                    createTrackerPageEvent(config, "LogIn", "logInFailure", loginHashObject);
                } else if (config.errorMsgs[i].code == "user.already.exists") {
                    loginHashObject.pd4 = "User already exists";
                    createTrackerPageView(config, "register", "RegisterPage");
                    createTrackerPageEvent(config, "register", "registerFailure", loginHashObject);
                } else if (config.errorMsgs[i].code.indexOf("error.register") >= 0) {
                    loginHashObject.pd4 = "Register system failure";
                    createTrackerPageView(config, "register", "RegisterPage");
                    createTrackerPageEvent(config, "register", "registerFailure", loginHashObject);
                } else if ((config.errorMsgs[i].code == "password.resetkey.invalid") || (config.errorMsgs[i].code == "password.already.used")) {
                    createTrackerPageView(config, "LogIn", "LoginPage");
                    createTrackerPageEvent(config, "reset-password", "resetPasswordFailure");
                } else {
                    createTrackerPageEvent(config, "LogIn", "logInFailure", loginHashObject);
                }
            }

        }
    }

    var app = UWA.createElement("div", {
        "id": "app"
    });
	
	var customImageUrl = config.imgResourceUrl ? config.imgResourceUrl : undefined;
    var simpleLayout = new UWA.dsp.component.SimpleLayout({
		custom_background_image_url : customImageUrl		
	});

    panel = new UWA.dsp.component.SimplePanel({
        className: "simple-login-panel",
        title: config.loginTitle || i18n.translate("commons.login.welcome.title.default"),
        titleNls: config.loginTitleNls || 'Welcome to the <b>3D</b>EXPERIENCE platform',
        description: config.loginSubTitle || i18n.translate("commons.login.welcome.subtitle.default"),
        descriptionNls:  config.loginSubTitleNls || 'Sign in to continue',
        logo: true
    });

    if (!navigator.cookieEnabled) {
        cookiesMsg = UWA.createElement("div", {
            "class": "dddxp-cookieless",
            "data-dsp-i18n": "commons.label.cookies",
            text: "Cookies must be enabled to use this service."
        });
        cookiesMsg.inject(document.body);
        return;
    }

    function toggleHiddenCSS(descriptor, hide) {
        var e = UWA.Element.getElement.call(document.body, descriptor);
        if (e !== null) {
            if (hide && !e.hasClassName("hidden")) {
                e.addClassName("hidden");
            } else if (!hide && e.hasClassName("hidden")) {
                e.removeClassName("hidden");
            }
        }
    }

    function toggleRegisterPanel() {
        createTrackerPageView(config, "register", "RegisterPage");
        window.location.hash = 'register';
        var registerTitle = config.registerTitle || "commons.account.create.title";
        panel.updateTitle(registerTitle, "Create an account");
        var registerDescription = config.registerSubTitle || "commons.account.create.subtitle";
		panel.updateDescription(registerDescription, "Create your account to continue");
        if (!registerFormLoaded) {
            registerFormLoaded = true;
            UWA.Json.request(config.getRegisterFormUrl,
                {
                    onComplete: function (data) {
                        onLoadRegisterForm(data);
                        toggleHiddenCSS('.login-form', true);
                        toggleHiddenCSS('.login-links', true);
                        toggleHiddenCSS('.register-form', false);
                        toggleHiddenCSS('.register-links', false);
                        toggleHiddenCSS('.welcome', true);
                        i18n.translateDocument();
                    }
                });
        } else {
            toggleHiddenCSS('.login-form', true);
            toggleHiddenCSS('.login-links', true);
            toggleHiddenCSS('.register-form', false);
            toggleHiddenCSS('.register-links', false);
            toggleHiddenCSS('.welcome', true);
            i18n.translateDocument();
        }
    }

    function toggleLoginPanel() {
        createTrackerPageView(config, "LogIn", "LoginPage");
        window.location.hash = 'login';
        var loginTitle = config.loginTitle || "commons.login.welcome.title.default";
        panel.updateTitle(loginTitle, 'Welcome to the <b>3D</b>EXPERIENCE platform');
        var loginDescription = config.loginSubTitle || "commons.login.welcome.subtitle.default";
		panel.updateDescription(loginDescription, 'Sign in to continue');
        toggleHiddenCSS('.login-form', false);
        toggleHiddenCSS('.login-links', false);
        toggleHiddenCSS('.register-form', true);
        toggleHiddenCSS('.register-links', true);
        if (localStorage.getItem('lastUser')) {
            toggleHiddenCSS('.welcome', true);     /* Do not show welcome message (In case of new UI)*/
            toggleHiddenCSS('.login-legend', true); /* hide the login legend */
        }
        i18n.translateDocument();
    }

    /* crappy hack to listen for anchor change, required for the links in the error message */
    window.addEventListener('hashchange', function (e) {
        var hash = window.location.hash.substr(1);
        if (hash === 'register') {
            panel.getContent().getElement('.error-messages').remove()
            toggleRegisterPanel();
        } else if (hash === 'login') {
            panel.getContent().getElement('.error-messages').remove()
            toggleLoginPanel();
        }
    });


    if (!config.doNotShowLoginForm) {
        // This basically checks if there is a user in Local Storage then it will directly hit the /login_identifier?username=xx endpoint
        let url = config.url;
        if (config.errorMsgs.length == 0
            && localStorage.getItem("lastUser") !== null
            && url.endsWith("/login_identifier")
            && config.autoSubmitToLoginIdentifier == "true") {
            window.location.href = url.concat("?username=", localStorage.getItem("lastUser"));
            return;
        }
        loginForm = createLoginForm({
            url: config.url,
            loginTicket: config.lt,
            username: config.username || localStorage.getItem("lastUser"),//This will also avoid chrome auto-filling the last user's creds if saved
            authorizeRememberMe: config.authorizeRememberMe,
            displayCaptcha: config.needsCaptcha,
            captchaType: config.captchaType,
            footnoteNls: config.footnoteNls,
            needHelpUrl: config.needHelpUrl,
            displayPassword: config.displayPassword,
            oidcLoginUiEnabled: config.oidcLoginUiEnabled,
            previousPassportUrl: config.previousPassportUrl,
            showLoginWithSSOButton: config.showLoginWithSSOButton,
            delegatedAuthSSOUrl: config.delegatedAuthSSOUrl
        });
        addMessagesToPanel(config, panel);
        loginTicketExpiredMsg = UWA.createElement("div", {
            "class": "lt-expired-error hidden",
            html: [{
                tag: "p",
                "class": "lt-expired-msg",
                "data-dsp-i18n": "login.ticket.expired"
            }, {
                tag: "a",
                href: "#",
                "class": "lt-expired-btn",
                "data-dsp-i18n": "commons.action.log.again",
                tabindex: 0,
                events: {
                    "click": function (event) {
                        createTrackerPageView(config, "LogIn", "LoginPage");
                        UWA.Event.stop(event);
                        window.location.reload(false);
                    }
                }
            }]
        });
        linkItems.push({
            tag: "div",
            id: "iAmNotLink",
            "class": "hidden",
            html: [{
                tag: "a",
                html: [{
                    tag: "span",
                    "data-dsp-i18n": "commons.login.iamnot",
                    text: "I am not"
                }, {
                    tag: "span",
                    text: " "
                }, {
                    tag: "span",
                    "class": "username"
                }],
                href: "#",
                events: {
                    click: function (ev) {
                        UWA.Event.stop(ev);

                        toggleHiddenCSS('.field.username', false); /* show the username field */
                        toggleHiddenCSS('#iAmNotLink', true); /* hide the "I am not" link */
                        toggleHiddenCSS('.welcome', true); /* hide the welcome message */
                        toggleHiddenCSS('.login-legend', false); /* show the login legend */

                        // Fix Edge bug on which placeholder is not erased (specific Windows 10 1709)
                        UWA.Element.getElement.call(document.body, ".username.uwa-input").focus();
                        UWA.Element.getElement.call(document.body, ".username.uwa-input").value = "";
                        if (localStorage) {
                            localStorage.removeItem('lastUser');
                        }
                    }
                }
            }]
        });
	linkItems.push({
            tag: "div",
            id: "SafeNotice",
            html: [{
                tag: "span",
                text: "本平台为内网非涉密平台，严禁处理、传输国家秘密"
                }]
        });
        registerFormLoaded = false;
        if (config.registerUrl) {
            registerContainer = UWA.createElement("div");
            panel.addContentElement(registerContainer);

            if(!config.displayPassword || !config.oidcLoginUiEnabled) {
                linkItems.push({
                    tag: "div",
                    html: [{
                        tag: "span",
                        "data-dsp-i18n": "commons.dont-have-an-account-yet-question",
                        text: "Don't have an account yet?",
                    },{
                        tag: "span",
                        text: " ",
                    },{
                        tag: "a",
                        href: "#",
                        name: "go-register",
                        "class": "go-register",
                        "data-dsp-i18n": "commons.account.create",
                        text: "Create your account",
                        tabindex: 0,
                        events: {
                            click: function (event) {
                                toggleRegisterPanel();
                                UWA.Event.stop(event);
                            }
                        }
                    }]
                });
            }
        }

        if (config.forgotPasswordUrl) {
            linkItems.push({
                tag: "div",
                html: [{
                    tag: "a",
                    href: config.forgotPasswordUrl,
                    "data-dsp-i18n": "commons.password.forgot",
                    text: "Forgot your password?",
                    tabindex: 0
                }]
            });
        }

        if (config.footnoteNls !== undefined
            && config.footnoteNls !== ""
            && config.footnoteNls !== null) {
            linkItems.push({
                tag: "div",
                "class": "login-asterisk-div",
                html: [{
                    tag: "span",
                    text: "",
                    "data-dsp-i18n": config.footnoteNls,
                    "class": "login-asterisk-text"
                }]
            });
        }

        needHelpLink = UWA.createElement("div");
        if (config.needHelpUrl !== undefined
            && config.needHelpUrl !== ""
            && config.needHelpUrl !== null) {
            needHelpLink = UWA.createElement("div", {
                "class": "login-needhelp-div",
                html: [{
                    tag: "a",
                    href: config.needHelpUrl,
                    "data-dsp-i18n": "commons.login.help",
                    text: "Help",
                    "target": "Support"
                }]

            });
        }

        links = new UWA.Element("div", {
            "class": "login-links",
            html: linkItems
        });
        if (config.needsCaptcha) {
            if (config.captchaType === "legacyCaptcha") {
                captcha = new UWA.dsp.component.CaptchaDisplay({
                    captchaImgUrl: config.captchaImgUrl
                });
                captcha.inject(loginForm.getContent().getElement(".field.captcha"), "before");
            } else {
                require(["recaptcha"], function (recaptcha) {
                    captcha = UWA.createElement('div', {
                        "class": "g-recaptcha",
                        "data-sitekey": config.captchaClientId
                    });
                    captcha.inject(loginForm.getContent().getElement(".field.password"), "after");
                });
            }
        }
    }
    loginFormElt = loginForm.getContent();

    if (!config.doNotShowLoginForm) {
        panel.addContentElement(UWA.createElement("div", {
            "class": "welcome",
            html: [{
                tag: "span",
                "data-dsp-i18n": "commons.login.welcome",
                text: "Welcome"
            }, {
                tag: "span",
                text: " "
            }, {
                tag: "span",
                id: "userNameWelcome"
            }]
        }));

        panel.addContentElement(loginForm);
        panel.addContentElement(loginTicketExpiredMsg);
        panel.addContentElement(links);
        // For adding Continue with SSO button
        if(!config.displayPassword && config.showLoginWithSSOButton === "true") {
            var loginSSOContainer = (UWA.createElement("div", {
                "class": "centerDiv"
            }));

         loginSSOContainer.addContent(UWA.createElement("div", {
            "class": "orSeparator",
            styles: {
               'margin-top': '25px'
            },
            html: {
                tag: "span",
                "data-dsp-i18n": "commons.or.separator",
                text: i18n.translate("commons.or.separator")
            }
            }));
           var connectPanelsso = (UWA.createElement("div", {
                                    "class": "connectPanel"
           }));
           var connectWithSSO = new UWA.createElement("button", {
                "type": "button",
                "class": "btn-primary btn btn-root btn-with-icon",
                "text": "field.sso.button.label",
                "data-dsp-i18n": "field.sso.button.label",
                styles: {
                    'margin-top': '20px',
                    width: '100%'
                },
                events: {
                    click: function (e) {
                        window.location.href = config.delegatedAuthSSOUrl;
                    }
                }
            });
            connectPanelsso.addContent(connectWithSSO);
            loginSSOContainer.addContent(connectPanelsso);
            panel.addContentElement(loginSSOContainer);
        }

        var socialContainer = (UWA.createElement("div", {
            "class": "centerDiv"
        }));

        var isSamlButtonConfigured = config.samlButtonText !== undefined && config.samlButtonLabel !== undefined && config.samlActivationUrl !== undefined;
        if (config.availableSN.gplus !== undefined ||
            config.availableSN.fb !== undefined || (config.samlEnabled && isSamlButtonConfigured)) {
            require(["DS/W3DPassport/login/socialLinks"], function (socialLinks) {
                socialContainer.addContent(UWA.createElement("div", {
                    "class": "orSeparator",
                    html: {
                        tag: "span",
                        "data-dsp-i18n": "commons.or.separator",
                        text: i18n.translate("commons.or.separator")
                    }
                }));
                if (config.samlEnabled == true && isSamlButtonConfigured) {
                    connectPanel = (UWA.createElement("div", {
                        "class": "connectPanel"
                    }));
                    connectWithSAML = new UWA.createElement("button", {
                        "type": "button",
                        "class": "medium uwa-button uwa-input connectButton",
                        "text": config.samlButtonText,
                        "data-dsp-i18n": config.samlButtonLabel,
                        events: {
                            click: function (e) {
                                window.location.href = config.samlActivationUrl;
                            }
                        }
                    });
                    connectPanel.addContent(connectWithSAML);
                    socialContainer.addContent(connectPanel);
                }

                if (config.availableSN.gplus !== undefined || config.availableSN.fb !== undefined) {
                    socialContainer.addContent(socialLinks.createSocialButtons({
                        facebook: config.availableSN.fb,
                        gplus: config.availableSN.gplus,
                        byme: config.availableSN.byme
                    }));
                }
            });
        }
        panel.addContentElement(socialContainer);
        createLanguagePicker(i18n, config.cookieDomain).inject(panel.getFooter());
        needHelpLink.inject(panel.getFooter());
    }
    // remove the welcome div per default
    UWA.Element.getElement.call(panel.getContent(), ".welcome").addClassName("hidden");
    // if (location.hash !== '#reset_login') {
    //     rememberLastUser.load(loginFormElt, UWA.Element.getElement.call(panel.getContent(), "#userNameWelcome"),
    //         UWA.Element.getElement.call(panel.getContent(), "#iAmNotLink"),
    //         UWA.Element.getElement.call(panel.getContent(), ".welcome"));
    // }

    loginFormElt.addEvent("submit", function (evt) {

        if (config.trackerJs !== undefined
            && config.trackerJs === "true" && window.location === window.parent.location) {
            //Hack needed to have synchrone call of tracker API
            arguments[0].preventDefault();

            //Filter if it is a login with username or email.
            // pd1 must be the type of connexion (regular, 2FA...)
            // pd2 must be the hash corresponding to email connexion
            // pd3 must be the hash corresponding to username connexion
            if ((loginFormElt.getElement(".username input").value).includes('@')) {
                var loginHashObject = {
                    pd1: 'regular',
                    pd2: Utils.sha256(loginFormElt.getElement(".username input").value)
                };
            } else {
                var loginHashObject = {
                    pd1: 'regular',
                    pd2: '',
                    pd3: Utils.sha256(loginFormElt.getElement(".username input").value)
                };
            }

            if (loginFormElt.getElementsByClassName("invalid").length == 0
                && loginFormElt.getElementsByClassName("invalid-password").length == 0) {
                var callbackFct = function () {
                    loginFormElt.submit();
                };
            }

            if (config.needsCaptcha) {
                createTrackerPageEvent(config, "captcha", "captchaTentative", loginHashObject, callbackFct);
            }
            createTrackerPageEvent(config, "LogIn", "logInTentative", loginHashObject, callbackFct);
        }
        rememberLastUser.save(loginFormElt);
    });

    displayHidePassword(loginFormElt.elements["password"]);


    if (config.gdprEnabled !== undefined && config.gdprEnabled === true) {
        var manageCookiesLink = UWA.createElement("div", {
            html: [{
                tag: "a",
                href: "#",
                "data-dsp-i18n": "commons.gdpr.managecookies",
                text: "Manage cookies",
                events: {
                    click: function (ev) {
                        UWA.Event.stop(ev);
                        require(["DS/GDPR/GDPR"], function (GDPR) {
                            GDPR.start();
                        });
                    }
                }
            }]
        });
        manageCookiesLink.inject(panel.getFooter());
    }

    simpleLayout.addContent(panel);
    simpleLayout.inject(app);

    app.inject(document.body);
    if (location.hash === '#register') {
        var registerLink = UWA.Element.getElement.call(document.body, '.login-links .go-register');
        if (registerLink !== null) {
            registerLink.triggerEvent('click');
        }
    }
    if (location.href.contains("pairing_init=true")) {
        createModalPanelPairing(document.body, toggleLoginPanel, toggleRegisterPanel);
    }

    var usernameElmt = document.getElementById('username');
    var passwordElmt = document.getElementById('password');
    if(isIE11()) {
        // Delay the focus logic to allow IE11 to fully render the page
        // Using setTimeout(0) defers the focus operation until the browser finishes rendering and applying default behaviors like autofocus.
        setTimeout(function() {
            // Ensure username field is empty
            if (usernameElmt && usernameElmt.value === '') {
                // Focus on the username field if it's empty
                usernameElmt.focus();
            } else {
                // Otherwise, focus on the password field
                if (passwordElmt) {
                    passwordElmt.focus();
                }
            }
        }, 0); // Delay to allow the page to settle
    } else {
         if (usernameElmt && usernameElmt.value === '') {
             // Focus on the username field only if it's empty
             usernameElmt.focus();
         } else {
             // Otherwise, focus on the password field
             if (passwordElmt) {
                 passwordElmt.focus();
             }
         }
    }


    if (!config.doNotShowLoginForm) {
        loginTicketTimeoutMsg(config.loginTicketUrl);
    }


    if (config.gdprEnabled !== undefined && config.gdprEnabled === true){
      require(["DS/GDPR/GDPR"], function (GDPR) {
        GDPR.init();
      });
    }


    i18n.translateDocument();

});

