package blink;

/* BigRational.java -- dynamically sized big rational numbers.
**
** Copyright (C) 2002 Eric Laroche.  All rights reserved.
**
** @author Eric Laroche <laroche@lrdev.com>
** @version @(#)$Id: BigRational.java,v 1.1 2006/06/17 11:08:10 lauro Exp $
**
** This program is free software;
** you can redistribute it and/or modify it.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
**
*/

// package com.lrdev.bn;
// package java.math; // where it'd belong to.

import java.math.BigInteger;

// Compiles and runs and passes tests on at least:
// * jdk 1.4.0
// * jdk 1.3.1_03
// * jdk 1.2.2_05a
// * jdk 1.1.6

/** BigRational: dynamically sized immutable arbitrary-precision rational numbers.
*
* @author Eric Laroche <laroche@lrdev.com>, June 2002
* @version @(#)$Id: BigRational.java,v 1.1 2006/06/17 11:08:10 lauro Exp $
*
* <P>
* BigRational provides most operations needed in rational number space
* calculations, including multiplication and division.
*
* <P>
* BigRational uses Sun's java.math.BigInteger (JDK 1.1 and later).
*
* <P>
* Binary operations (e.g. add, multiply) calculate their result from a
* BigRational object ('this') and one argument (typically called 'that'),
* returning a new immutable BigRational object.
* Both the original object and the argument are left unchanged (hence
* immutable).
*
* <P>
* Unary operations (e.g. negate, invert) calculate their result from the
* BigRational object ('this'), returning a new immutable BigRational object.
* The original object is left unchanged.
*
* <P>
* Some commonly used short function names (abs, ceil, div, inv, max, min,
* mod, mul, neg, pow, rem, sign, sub, trunc) are additionally defined as
* aliases to to the full function names (absolute, ceiling, divide, invert,
* maximum, minimum, modulus, multiply, negate, power, remainder, signum,
* subtract, truncate).
* This makes the interface somewhat fatter.
*
* <P>
* BigRational implements private proxy functions for BigInteger functionality,
* including scanning and multiplying, to enhance speed and to realize fast
* checks for common values (1, 0).
*
* <P>
* Usage samples:
* <SMALL><PRE>
* 	BigRational("-21/35"): rational -3/5
* 	BigRational("/3"): rational 1/3
* 	BigRational("3.4"): rational 17/5
* 	BigRational(".7"): 0.7, rational 7/10
* 	BigRational("f/37", 0x10): 3/11
* 	BigRational("f.37", 0x10): 3895/256
* 	BigRational("-dcba.efgh", 23): -46112938320/279841
* 	BigRational("1234.5678")).toStringDot(6): "1234.567800"
* 	BigRational("1234.5678")).toStringDot(2): "1234.57"
* 	BigRational("1234.5678")).toStringDot(-2): "1200"
* </PRE></SMALL>
*
* <P>
* The BigRational source and documentation can typically be found at
* the author's site, at
* <A HREF="http://www.lrdev.com/lr/java/BigRational.java"
* >http://www.lrdev.com/lr/java/BigRational.java</A> and
* <A HREF="http://www.lrdev.com/lr/java/BigRational.html"
* >http://www.lrdev.com/lr/java/BigRational.html</A>.
*
*/
public class BigRational
//	extends Number // [don't provide float values.]
//	implements Comparable // [only jdk-1.2 and later.]
//	implements Cloneable
{
        /** Numerator.
        * Numerator may be negative.
        * Numerator may be zero (in which case m_q is one).
        */
        private BigInteger m_n;

        /** Denominator (quotient).
        * Denominator is never negative and never zero.
        */
        private BigInteger m_q;

        /** Default radix, used in string printing and scanning, 10.
        * Default radix is decimal, of course.
        */
        public final static int DEFAULT_RADIX = 10;

        // note: following constants can't be constructed using bigIntegerValueOf().
        // that one _uses_ the constants.

        /** Constant internally used, for convenience and speed.
        * Used as zero numerator.
        * Used for checks.
        */
        private final static BigInteger BIG_INTEGER_ZERO = BigInteger.valueOf(0);

        /** Constant internally used, for convenience and speed.
        * Used as neutral denominator.
        * Used for checks.
        */
        private final static BigInteger BIG_INTEGER_ONE = BigInteger.valueOf(1);

        /** Constant internally used, for convenience and speed.
        * Used for checks.
        */
        private final static BigInteger BIG_INTEGER_MINUS_ONE = BigInteger.valueOf(-1);

        /** Constant internally used, for convenience and speed.
        * Used in rounding zero numerator.
        * _Not_ used for checks.
        */
        private final static BigInteger BIG_INTEGER_TWO = BigInteger.valueOf(2);

        /** Constant internally used, for convenience and speed.
        * _Not_ used for checks.
        */
        private final static BigInteger BIG_INTEGER_MINUS_TWO = BigInteger.valueOf(-2);

        /** Constant internally used, for convenience and speed.
        * Corresponds to DEFAULT_RADIX, used in reading, scaling and printing.
        * _Not_ used for checks.
        */
        private final static BigInteger BIG_INTEGER_TEN = BigInteger.valueOf(10);

        /** Constant internally used, for convenience and speed.
        * Used in reading, scaling and printing.
        * _Not_ used for checks.
        */
        private final static BigInteger BIG_INTEGER_SIXTEEN = BigInteger.valueOf(16);

        /** Construct a BigRational from numerator and denominator.
        * Both n and q may be negative.
        * n/q may be denormalized.
        */
        public BigRational(BigInteger n, BigInteger q)
        {
                if (bigIntegerZerop(q)) {
                        throw new NumberFormatException("quotient zero");
                }

                m_n = n;
                m_q = q;

                normalize();
        }

        /** Construct a BigRational from a big number integer,
        * denominator is 1.
        */
        public BigRational(BigInteger n)
        {
                this(n, BIG_INTEGER_ONE);
        }

        /** Construct a BigRational from long fix number integers
        * representing numerator and denominator.
        */
        public BigRational(long n, long q)
        {
                this(bigIntegerValueOf(n), bigIntegerValueOf(q));
        }

        /** Construct a BigRational from a long fix number integer.
        */
        public BigRational(long n)
        {
                this(bigIntegerValueOf(n), BIG_INTEGER_ONE);
        }

        // note: byte/short/int implicitly upgraded to long,
        // so we don't implement BigRational(int,int) et al.

        // note: "[+-]i.fEe" not supported.
        // we wouldn't want to distinguish exponent-'e' from large-base-digit-'e' anyway
        // (at least not at this place).

        /** Construct a BigRational from a string representation,
        * the supported formats are "[+-]d/[+-]q", "[+-]i.f", "[+-]i".
        */
        public BigRational(String s, int radix)
        {
                if (s == null) {
                        throw new NumberFormatException("null");
                }

                int slash = s.indexOf('/');
                int dot = s.indexOf('.');

                if (slash != -1 && dot != -1) {
                        throw new NumberFormatException("can't have both slash and dot");
                }

                if (slash != -1) {

                        // "[+-]d/[+-]q"
                        String d = s.substring(0, slash);
                        String q = s.substring(slash + 1);

                        // check for multiple signs or embedded signs
                        checkNumberFormat(d);

                        // skip '+'
                        if (d.length() > 0 && d.charAt(0) == '+') {
                                d = d.substring(1);
                        }

                        // handle "/x" as "1/x"
                        // note: "1" and "-1" are treated special in newBigInteger().
                        if (d.equals("")) {
                                d = "1";
                        } else if (d.equals("-")) {
                                d = "-1";
                        }

                        // note: here comes some code duplicated from numerator handling.
                        // it seems however clearer not to invent a unobvious abstraction
                        // to handle these two cases.

                        // check for multiple signs or embedded signs
                        checkNumberFormat(q);

                        // skip '+'
                        if (q.length() > 0 && q.charAt(0) == '+') {
                                q = q.substring(1);
                        }

                        // handle "x/" as "x"
                        // note: "1" and "-1" are treated special in newBigInteger().
                        if (q.equals("")) {
                                q = "1";
                        } else if (q.equals("-")) {
                                q = "-1";
                        }

                        m_n = newBigInteger(d, radix);
                        m_q = newBigInteger(q, radix);

                } else if (dot != -1) {

                        // "[+-]i.f"
                        String i = s.substring(0, dot);
                        String f = s.substring(dot + 1);

                        // check for multiple signs or embedded signs
                        checkNumberFormat(i);

                        // skip '+'
                        if (i.length() > 0 && i.charAt(0) == '+') {
                                i = i.substring(1);
                        }

                        // handle '-'
                        boolean negt = false;
                        if (i.length() > 0 && i.charAt(0) == '-') {
                                negt = true;
                                i = i.substring(1);
                        }

                        // handle ".x" as "0.x" ("." as "0.0")
                        // note: "0" is treated special in newBigInteger().
                        if (i.equals("")) {
                                i = "0";
                        }

                        // check for signs
                        checkFractionFormat(f);

                        // handle "x." as "x.0" ("." as "0.0")
                        // note: "0" is treated special in newBigInteger().
                        if (f.equals("")) {
                                f = "0";
                        }

                        BigInteger iValue = newBigInteger(i, radix);
                        BigInteger fValue = newBigInteger(f, radix);

                        int scale = f.length();
                        checkRadix(radix);
                        BigInteger fq = bigIntegerPow(bigIntegerValueOf(radix), scale);

                        m_n = bigIntegerMultiply(iValue, fq).add(fValue);

                        if (negt) {
                                m_n = m_n.negate();
                        }

                        m_q = fq;

                } else {

                        // "[+-]i".  [not just delegating to BigInteger.]

                        String i = s;

                        // check for multiple signs or embedded signs
                        checkNumberFormat(i);

                        // skip '+'.  BigInteger doesn't handle these.
                        if (i.length() > 0 && i.charAt(0) == '+') {
                                i = i.substring(1);
                        }

                        // handle "" as "0"
                        // note: "0" is treated special in newBigInteger().
                        if (i.equals("") || i.equals("-")) {
                                i = "0";
                        }

                        m_n = newBigInteger(i, radix);
                        m_q = BIG_INTEGER_ONE;
                }

                normalize();
        }

        /** Construct a BigRational from a string representation, with default radix,
        * the supported formats are "[+-]d/[+-]q", "[+-]i.f", "[+-]i".
        */
        public BigRational(String s)
        {
                this(s, DEFAULT_RADIX);
        }

        /** Construct a BigRational from an unscaled value by scaling.
        */
        public BigRational(BigInteger unscaledValue, int scale, int radix)
        {
                boolean negt = (scale < 0);
                if (negt) {
                        scale = -scale;
                }

                checkRadix(radix);
                BigInteger scaleValue = bigIntegerPow(bigIntegerValueOf(radix), scale);

                if (!negt) {
                        m_n = unscaledValue;
                        m_q = scaleValue;
                } else {
                        m_n = bigIntegerMultiply(unscaledValue, scaleValue);
                        m_q = BIG_INTEGER_ONE;
                }

                normalize();
        }

        /** Construct a BigRational from an unscaled value by scaling, default radix.
        */
        public BigRational(BigInteger unscaledValue, int scale)
        {
                this(unscaledValue, scale, DEFAULT_RADIX);
        }

        /** Construct a BigRational from an unscaled fix number value by scaling.
        */
        public BigRational(long unscaledValue, int scale, int radix)
        {
                this(bigIntegerValueOf(unscaledValue), scale, radix);
        }

        // can't have public BigRational(long unscaledValue, int scale)
        // as alias for BigRational(unscaledValue, scale, DEFAULT_RADIX);
        // it's too ambigous with public BigRational(long d, long q).

        /** Normalize BigRational.
        * Denominator will be positive, nummerator and denominator will have
        * no common divisor.
        * BigIntegers -1, 0, 1 will be set to constants for later comparision speed.
        */
        private void normalize()
        {
                // note: don't call anything that depends on a normalized this.
                // i.e.: don't call most (or all) of the BigRational methods.

                if (m_n == null || m_q == null) {
                        throw new NumberFormatException("null");
                }

                // [these are typically cheap.]
                int ns = m_n.signum();
                int qs = m_q.signum();

                // note: we don't throw on qs==0.  that'll be done elsewhere.
                // if (qs == 0) {
                //	throw new NumberFormatException("quotient zero");
                // }

                if (ns == 0 && qs == 0) {
                        // [both for speed]
                        m_n = BIG_INTEGER_ZERO;
                        m_q = BIG_INTEGER_ZERO;
                        return;
                }

                if (ns == 0) {
                        m_q = BIG_INTEGER_ONE;
                        // [for speed]
                        m_n = BIG_INTEGER_ZERO;
                        return;
                }

                if (qs == 0) {
                        m_n = BIG_INTEGER_ONE;
                        // [for speed]
                        m_q = BIG_INTEGER_ZERO;
                        return;
                }

                // check the frequent case of q==1, for speed.
                // note: this only covers the normalized-for-speed 1-case.
                if (m_q == BIG_INTEGER_ONE) {
                        // [for speed]
                        m_n = proxyBigInteger(m_n);
                        return;
                }

                // check the symmetric case too, for speed.
                // note: this only covers the normalized-for-speed 1-case.
                if ((m_n == BIG_INTEGER_ONE || m_n == BIG_INTEGER_MINUS_ONE) && qs > 0) {
                        // [for speed]
                        m_q = proxyBigInteger(m_q);
                        return;
                }

                // setup torn apart for speed
                BigInteger na = m_n;
                BigInteger qa = m_q;

                if (qs < 0) {
                        m_n = m_n.negate();
                        m_q = m_q.negate();
                        ns = -ns;
                        qs = -qs;

                        qa = m_q;
                        if (ns > 0) {
                                na = m_n;
                        }

                } else {
                        if (ns < 0) {
                                na = m_n.negate();
                        }
                }

                BigInteger g = na.gcd(qa);

                if (!bigIntegerOnep(g)) {
                        m_n = m_n.divide(g);
                        m_q = m_q.divide(g);
                }

                // for [later] speed
                m_n = proxyBigInteger(m_n);
                m_q = proxyBigInteger(m_q);
        }

        /** Check constraints on radixes.
        * Radix may not be negative or less than two.
        */
        private static void checkRadix(int radix)
        {
                if (radix < 0) {
                        throw new NumberFormatException("radix negative");
                }

                if (radix < 2) {
                        throw new NumberFormatException("radix too small");
                }
        }

        /** Check some of the integer format constraints.
        */
        private static void checkNumberFormat(String s)
        {
                // "x", "-x", "+x", "", "-", "+"

                if (s == null) {
                        throw new NumberFormatException("null");
                }

                // note: 'embedded sign' catches both-signs cases too.

                int p = s.indexOf('+');
                int m = s.indexOf('-');

                int pp = (p == -1 ? -1 : s.indexOf('+', p + 1));
                int mm = (m == -1 ? -1 : s.indexOf('-', m + 1));

                if ((p != -1 && p != 0) || (m != -1 && m != 0) || pp != -1 || mm != -1) {
                        // embedded sign.  this covers the both-signs case.
                        throw new NumberFormatException("embedded sign");
                }
        }

        /** Check number format for fraction part.
        */
        private static void checkFractionFormat(String s)
        {
                if (s == null) {
                        throw new NumberFormatException("null");
                }

                if (s.indexOf('+') != -1 || s.indexOf('-') != -1) {
                        throw new NumberFormatException("sign in fraction");
                }
        }

        /** Proxy to BigInteger.ValueOf().
        * Speeds up comparisions by using constants.
        */
        private static BigInteger bigIntegerValueOf(long n)
        {
                // return the internal constants used for checks if possible.

                // check whether it's outside int range.
                // actually check a much narrower range, fitting the switch below.
                if (n < -16 || n > 16) {
                        return BigInteger.valueOf(n);
                }

                // jump table, for speed
                switch ((int)n) {

                case 0 :
                        return BIG_INTEGER_ZERO;

                case 1 :
                        return BIG_INTEGER_ONE;

                case -1 :
                        return BIG_INTEGER_MINUS_ONE;

                case 2 :
                        return BIG_INTEGER_TWO;

                case -2 :
                        return BIG_INTEGER_MINUS_TWO;

                case 10 :
                        return BIG_INTEGER_TEN;

                case 16 :
                        return BIG_INTEGER_SIXTEEN;

                }

                return BigInteger.valueOf(n);
        }

        /** Convert BigInteger to its proxy.
        * Speeds up comparisions by using constants.
        */
        private static BigInteger proxyBigInteger(BigInteger n)
        {
                // note: these tests are quite expensive,
                // so they should be minimized to a reasonable amount.

                // there is a priority order in the tests:
                // 1, 0, -1.

                // two layer testing.
                // cheap tests first.

                if (n == BIG_INTEGER_ONE) {
                        return n;
                }

                if (n == BIG_INTEGER_ZERO) {
                        return n;
                }

                if (n == BIG_INTEGER_MINUS_ONE) {
                        return n;
                }

                // more expensive tests later.

                if (n.equals(BIG_INTEGER_ONE)) {
                        return BIG_INTEGER_ONE;
                }

                if (n.equals(BIG_INTEGER_ZERO)) {
                        // [typically not reached from normalize().]
                        return BIG_INTEGER_ZERO;
                }

                if (n.equals(BIG_INTEGER_MINUS_ONE)) {
                        return BIG_INTEGER_MINUS_ONE;
                }

                // note: BIG_INTEGER_TWO et al. _not_ used for checks
                // and therefore not proxied _here_.  this speeds up tests.

                // not proxy-able
                return n;
        }

        /** Proxy to new BigInteger().
        * Speeds up comparisions by using constants.
        */
        private static BigInteger newBigInteger(String s, int radix)
        {
                // note: mind the radix.
                // however, 0/1/-1 are not a problem.

                // _often_ used strings (e.g. 0 for empty fraction and
                // 1 for empty denominator), for speed.

                if (s.equals("1")) {
                        return BIG_INTEGER_ONE;
                }

                if (s.equals("0")) {
                        return BIG_INTEGER_ZERO;
                }

                if (s.equals("-1")) {
                        return BIG_INTEGER_MINUS_ONE;
                }

                // note: BIG_INTEGER_TWO et al. _not_ used for checks
                // and therefore not proxied _here_.  this speeds up tests.

                return new BigInteger(s, radix);
        }

        /** BigInteger equality proxy.
        * For speed.
        */
        private static boolean bigIntegerEquals(BigInteger n, BigInteger m)
        {
                // first test is for speed.
                if (n == m) {
                        return true;
                }

                return n.equals(m);
        }

        /** Zero (0) value predicate.
        * For convenience and speed.
        */
        private static boolean bigIntegerZerop(BigInteger n)
        {
                // first test is for speed.
                if (n == BIG_INTEGER_ZERO) {
                        return true;
                }

                // well, this is also optimized for speed a bit.
                return (n.signum() == 0);
        }

        /** One (1) value predicate.
        * For convenience and speed.
        */
        private static boolean bigIntegerOnep(BigInteger n)
        {
                // first test is for speed.
                if (n == BIG_INTEGER_ONE) {
                        return true;
                }

                return bigIntegerEquals(n, BIG_INTEGER_ONE);
        }

        /** BigInteger multiply proxy.
        * For speed.
        * The more common cases of integers (q == 1) are optimized.
        */
        private static BigInteger bigIntegerMultiply(BigInteger n, BigInteger m)
        {
                // optimization: one or both operands are zero.
                if (bigIntegerZerop(n) || bigIntegerZerop(m)) {
                        return BIG_INTEGER_ZERO;
                }

                // optimization: second operand is one (i.e. neutral element).
                if (bigIntegerOnep(m)) {
                        return n;
                }

                // optimization: first operand is one (i.e. neutral element).
                if (bigIntegerOnep(n)) {
                        return m;
                }

                // default case.  [this would handle all cases.]
                return n.multiply(m);
        }

        /** Proxy to new BigInteger.pow().
        * For speed.
        */
        private static BigInteger bigIntegerPow(BigInteger n, int exponent)
        {
                // jump table, for speed.
                switch (exponent) {

                case 0 :
                        if (bigIntegerZerop(n)) {
                                throw new ArithmeticException("zero exp zero");
                        }
                        return BIG_INTEGER_ONE;

                case 1 :
                        return n;

                }

                return n.pow(exponent);
        }

        /** The constant zero (0).
        * [Constant name: see class BigInteger.]
        */
        public final static BigRational ZERO = new BigRational(0);

        /** The constant one (1).
        * [Name: see class BigInteger.]
        */
        public final static BigRational ONE = new BigRational(1);

        /** The constant minus-one (-1).
        */
        public final static BigRational MINUS_ONE = new BigRational(-1);

        /** Positive predicate.
        * For convenience.
        */
        private boolean positivep()
        {
                return (signum() > 0);
        }

        /** Negative predicate.
        * For convenience.
        */
        private boolean negativep()
        {
                return (signum() < 0);
        }

        /** Zero predicate.
        * For convenience and speed.
        */
        private boolean zerop()
        {
                // first test is for speed.
                if (this == ZERO) {
                        return true;
                }

                // well, this is also optimized for speed a bit.
                return (signum() == 0);
        }

        /** One predicate.
        * For convenience and speed.
        */
        private boolean onep()
        {
                // first test is for speed.
                if (this == ONE) {
                        return true;
                }

                return equals(ONE);
        }

        /** Integer predicate (quotient is one).
        * For convenience.
        */
        private boolean integerp()
        {
                return bigIntegerOnep(m_q);
        }

        /** BigRational string representation, format "[-]d[/q]".
        */
        public String toString(int radix)
        {
                String s = m_n.toString(radix);

                if (integerp()) {
                        return s;
                }

                String t = m_q.toString(radix);

                return s + "/" + t;
        }

        /** BigRational string representation, format "[-]d[/q]", default radix.
        * Default string representation.
        * Overwrites Object.toString().
        */
        public String toString()
        {
                return toString(DEFAULT_RADIX);
        }

        /** Dot-format "[-]i.f" string representation, with a precision.
        * Precision may be negative.
        */
        public String toStringDot(int precision, int radix)
        {
                checkRadix(radix);
                BigRational scaleValue = new BigRational(
                        bigIntegerPow(bigIntegerValueOf(radix),
                                (precision < 0 ? -precision : precision)));
                if (precision < 0) {
                        scaleValue = scaleValue.invert();
                }

                // default round mode.
                BigRational n = multiply(scaleValue).round();
                boolean negt = n.negativep();
                if (negt) {
                        n = n.negate();
                }

                String s = n.toString(radix);

                if (precision >= 0) {
                        // left-pad with '0'
                        while (s.length() <= precision) {
                                s = "0" + s;
                        }

                        int dot = s.length() - precision;
                        String i = s.substring(0, dot);
                        String f = s.substring(dot);

                        s = i;
                        if (f.length() > 0) {
                                s = s + "." + f;
                        }

                } else {
                        if (!s.equals("0")) {
                                // right-pad with '0'
                                for (int i = -precision; i > 0; i--) {
                                        s = s + "0";
                                }
                        }
                }

                // add sign
                if (negt) {
                        s = "-" + s;
                }

                return s;
        }

        /** Dot-format "[-]i.f" string representation, with a precision, default radix
        * Precision may be negative.
        */
        public String toStringDot(int precision)
        {
                return toStringDot(precision, DEFAULT_RADIX);
        }

        // note: there is no 'default' precision.

        /** Add two BigRationals and return a new BigRational.
        * [Name: see class BigInteger.]
        */
        public BigRational add(BigRational that)
        {
                // optimization: second operand is zero (i.e. neutral element).
                if (that.zerop()) {
                        return this;
                }

                // optimization: first operand is zero (i.e. neutral element).
                if (zerop()) {
                        return that;
                }

                // note: the calculated n/q may be denormalized,
                // implicit normalize() is needed.

                // optimization: same denominator.
                if (bigIntegerEquals(m_q, that.m_q)) {
                        return new BigRational(
                                m_n.add(that.m_n),
                                m_q);
                }

                // optimization: second operand is an integer.
                if (that.integerp()) {
                        return new BigRational(
                                m_n.add(that.m_n.multiply(m_q)),
                                m_q);
                }

                // optimization: first operand is an integer.
                if (integerp()) {
                        return new BigRational(
                                m_n.multiply(that.m_q).add(that.m_n),
                                that.m_q);
                }

                // default case.  [this would handle all cases.]
                return new BigRational(
                        m_n.multiply(that.m_q).add(that.m_n.multiply(m_q)),
                        m_q.multiply(that.m_q));
        }

        /** Add a BigRational and a long fix number integer and return a new BigRational.
        */
        public BigRational add(long that)
        {
                return add(new BigRational(that));
        }

        /** Subtract a BigRational from another (this) and return a new BigRational.
        * [Name: see class BigInteger.]
        */
        public BigRational subtract(BigRational that)
        {
                // optimization: second operand is zero.
                if (that.zerop()) {
                        return this;
                }

                // [not optimizing first operand being zero.]

                // note: the calculated n/q may be denormalized,
                // implicit normalize() is needed.

                // optimization: same denominator.
                if (bigIntegerEquals(m_q, that.m_q)) {
                        return new BigRational(
                                m_n.subtract(that.m_n),
                                m_q);
                }

                // optimization: second operand is an integer.
                if (that.integerp()) {
                        return new BigRational(
                                m_n.subtract(that.m_n.multiply(m_q)),
                                m_q);
                }

                // optimization: first operand is an integer.
                if (integerp()) {
                        return new BigRational(
                                m_n.multiply(that.m_q).subtract(that.m_n),
                                that.m_q);
                }

                // default case.  [this would handle all cases.]
                return new BigRational(
                        m_n.multiply(that.m_q).subtract(that.m_n.multiply(m_q)),
                        m_q.multiply(that.m_q));
        }

        /** Subtract a long fix number integer from this and return a new BigRational.
        */
        public BigRational subtract(long that)
        {
                return subtract(new BigRational(that));
        }

        /** An alias to subtract().
        */
        public BigRational sub(BigRational that)
        {
                return subtract(that);
        }

        /** An alias to subtract().
        */
        public BigRational sub(long that)
        {
                return subtract(that);
        }

        /** Multiply two BigRationals and return a new BigRational.
        * [Name: see class BigInteger.]
        */
        public BigRational multiply(BigRational that)
        {
                BigInteger n = bigIntegerMultiply(m_n, that.m_n);

                // optimization: one of the operands was zero.
                if (bigIntegerZerop(n)) {
                        return ZERO;
                }

                BigInteger q = bigIntegerMultiply(m_q, that.m_q);

                // note: the calculated n/q may be denormalized,
                // implicit normalize() is needed.

                return new BigRational(n, q);
        }

        /** Multiply a long fix number integer to this and return a new BigRational.
        */
        public BigRational multiply(long that)
        {
                return multiply(new BigRational(that));
        }

        /** An alias to multiply().
        */
        public BigRational mul(BigRational that)
        {
                return multiply(that);
        }

        /** An alias to multiply().
        */
        public BigRational mul(long that)
        {
                return multiply(that);
        }

        /** Divide a BigRational (this) through another and return a new BigRational.
        * [Name: see class BigInteger.]
        */
        public BigRational divide(BigRational that)
        {
                if (that.zerop()) {
                        throw new ArithmeticException("division by zero");
                }

                // note: the calculated n/q may be denormalized,
                // implicit normalize() is needed.

                return new BigRational(
                        bigIntegerMultiply(m_n, that.m_q),
                        bigIntegerMultiply(m_q, that.m_n));
        }

        /** Divide a BigRational (this) through a long fix number integer
        * and return a new BigRational.
        */
        public BigRational divide(long that)
        {
                return divide(new BigRational(that));
        }

        /** An alias to divide().
        */
        public BigRational div(BigRational that)
        {
                return divide(that);
        }

        /** An alias to divide().
        */
        public BigRational div(long that)
        {
                return divide(that);
        }

        /** Calculate a BigRational's integer power and return a new BigRational.
        * The integer exponent may be negative.
        */
        public BigRational power(int exponent)
        {
                boolean z = zerop();

                if (z && exponent == 0) {
                        throw new ArithmeticException("zero exp zero");
                }

                if (exponent == 0) {
                        return ONE;
                }

                // optimization
                if (z && exponent > 0) {
                        return ZERO;
                }

                // optimization
                if (exponent == 1) {
                        return this;
                }

                boolean negt = (exponent < 0);
                if (negt) {
                        exponent = -exponent;
                }

                BigInteger d = bigIntegerPow(m_n, exponent);
                BigInteger q = bigIntegerPow(m_q, exponent);

                // note: the calculated n/q are not denormalized,
                // implicit normalize() would not be needed.

                if (!negt) {
                        return new BigRational(d, q);
                } else {
                        return new BigRational(q, d);
                }
        }

        /** An alias to power().
        * [Name: see classes Math, BigInteger.]
        */
        public BigRational pow(int exponent)
        {
                return power(exponent);
        }

        /** Calculate the remainder of two BigRationals and return a new BigRational.
        * [Name: see class BigInteger.]
        * The remainder result may be negative.
        * The remainder is based on round down (towards zero) / truncate.
        * 5/3 == 1 + 2/3 (remainder 2), 5/-3 == -1 + 2/-3 (remainder 2),
        * -5/3 == -1 + -2/3 (remainder -2), -5/-3 == 1 + -2/-3 (remainder -2).
        */
        public BigRational remainder(BigRational that)
        {
                int s = signum();
                int ts = that.signum();

                if (ts == 0) {
                        throw new ArithmeticException("division by zero");
                }

                BigRational a = this;
                if (s < 0) {
                        a = a.negate();
                }

                // divisor's sign doesn't matter, as stated above.
                // this is also BigInteger's behavior, but don't let us be
                // dependent of a change in that.
                BigRational b = that;
                if (ts < 0) {
                        b = b.negate();
                }

                BigRational r = a.remainderOrModulusAbsolute(b);

                if (s < 0) {
                        r = r.negate();
                }

                return r;
        }

        /** Calculate the remainder of a BigRational and a long fix number integer
        * and return a new BigRational.
        */
        public BigRational remainder(long that)
        {
                return remainder(new BigRational(that));
        }

        /** An alias to remainder().
        */
        public BigRational rem(BigRational that)
        {
                return remainder(that);
        }

        /** An alias to remainder().
        */
        public BigRational rem(long that)
        {
                return remainder(that);
        }

        /** Calculate the modulus of two BigRationals and return a new BigRational.
        * The modulus result may be negative.
        * Modulus is based on round floor (towards negative).
        * 5/3 == 1 + 2/3 (modulus 2), 5/-3 == -2 + -1/-3 (modulus -1),
        * -5/3 == -2 + 1/3 (modulus 1), -5/-3 == 1 + -2/-3 (modulus -2).
        */
        public BigRational modulus(BigRational that)
        {
                int s = signum();
                int ts = that.signum();

                if (ts == 0) {
                        throw new ArithmeticException("division by zero");
                }

                BigRational a = this;
                if (s < 0) {
                        a = a.negate();
                }

                BigRational b = that;
                if (ts < 0) {
                        b = b.negate();
                }

                BigRational r = a.remainderOrModulusAbsolute(b);

                if (s < 0 && ts < 0) {
                        r = r.negate();
                } else if (ts < 0) {
                        r = r.subtract(b);
                } else if (s < 0) {
                        r = b.subtract(r);
                }

                return r;
        }

        /** Calculate the modulus of a BigRational and a long fix number integer
        * and return a new BigRational.
        */
        public BigRational modulus(long that)
        {
                return modulus(new BigRational(that));
        }

        /** An alias to modulus().
        * [Name: see class BigInteger.]
        */
        public BigRational mod(BigRational that)
        {
                return modulus(that);
        }

        /** An alias to modulus().
        */
        public BigRational mod(long that)
        {
                return modulus(that);
        }

        /** Remainder or modulus of non-negative values.
        * Helper function to remainder() and modulus().
        */
        private BigRational remainderOrModulusAbsolute(BigRational that)
        {
                int s = signum();
                int ts = that.signum();

                if (s < 0 || ts < 0) {
                        throw new IllegalArgumentException("negative values(s)");
                }

                if (ts == 0) {
                        throw new ArithmeticException("division by zero");
                }

                // optimization
                if (s == 0) {
                        return ZERO;
                }

                BigInteger n = bigIntegerMultiply(m_n, that.m_q);
                BigInteger q = bigIntegerMultiply(m_q, that.m_n);

                BigInteger r = n.remainder(q);
                BigInteger rq = bigIntegerMultiply(m_q, that.m_q);

                return new BigRational(r, rq);
        }

        /** Signum, -1, 0, or 1.
        * [Name: see class BigInteger.]
        */
        public int signum()
        {
                // note: m_q is positive.
                return m_n.signum();
        }

        /** An alias to signum().
        */
        public int sign()
        {
                return signum();
        }

        /** Return a new BigRational with the absolute value of this.
        */
        public BigRational absolute()
        {
                // note: m_q is positive.

                if (m_n.signum() >= 0) {
                        return this;
                }

                // optimization
                if (this == MINUS_ONE) {
                        return ONE;
                }

                // note: the calculated n/q are not denormalized,
                // implicit normalize() would not be needed.

                return new BigRational(m_n.negate(), m_q);
        }

        /** An alias to absolute().
        * [Name: see classes Math, BigInteger.]
        */
        public BigRational abs()
        {
                return absolute();
        }

        /** Return a new BigRational with the negative value of this.
        * [Name: see class BigInteger.]
        */
        public BigRational negate()
        {
                // optimization
                if (this == ZERO) {
                        return this;
                }

                // optimization
                if (this == ONE) {
                        return MINUS_ONE;
                }

                // optimization
                if (this == MINUS_ONE) {
                        return ONE;
                }

                // note: the calculated n/q are not denormalized,
                // implicit normalize() would not be needed.

                return new BigRational(m_n.negate(), m_q);
        }

        /** An alias to negate().
        */
        public BigRational neg()
        {
                return negate();
        }

        /** Return a new BigRational with the inverted (reciprocal) value of this.
        */
        public BigRational invert()
        {
                if (zerop()) {
                        throw new ArithmeticException("division by zero");
                }

                // optimization
                if (this == ONE || this == MINUS_ONE) {
                        return this;
                }

                // note: the calculated n/q are not denormalized,
                // implicit normalize() would not be needed.

                return new BigRational(m_q, m_n);
        }

        /** An alias to invert().
        */
        public BigRational inv()
        {
                return invert();
        }

        /** Return the minimal value of two BigRationals.
        */
        public BigRational minimum(BigRational that)
        {
                return (compareTo(that) <= 0 ? this : that);
        }

        /** Return the minimal value of a BigRational and a long fix number integer.
        */
        public BigRational minimum(long that)
        {
                return minimum(new BigRational(that));
        }

        /** An alias to minimum().
        * [Name: see classes Math, BigInteger.]
        */
        public BigRational min(BigRational that)
        {
                return minimum(that);
        }

        /** An alias to minimum().
        */
        public BigRational min(long that)
        {
                return minimum(that);
        }

        /** Return the maximal value of two BigRationals.
        */
        public BigRational maximum(BigRational that)
        {
                return (compareTo(that) >= 0 ? this : that);
        }

        /** Return the maximum value of a BigRational and a long fix number integer.
        */
        public BigRational maximum(long that)
        {
                return maximum(new BigRational(that));
        }

        /** An alias to maximum().
        * [Name: see classes Math, BigInteger.]
        */
        public BigRational max(BigRational that)
        {
                return maximum(that);
        }

        /** An alias to maximum().
        */
        public BigRational max(long that)
        {
                return maximum(that);
        }

        /** Compare object for equality.
        * Overwrites Object.equals().
        * Only some object types are allowed (see compareTo).
        * Never throws.
        */
        public boolean equals(Object object)
        {
                if (object == this) {
                        return true;
                }

                // delegate to compareTo(Object)
                try {
                        return (compareTo(object) == 0);
                } catch (ClassCastException e) {
                        return false;
                }
        }

        /** Hash code.
        * Overwrites Object.hashCode().
        */
        public int hashCode()
        {
                int dh = m_n.hashCode(), qh = m_q.hashCode();
                // dh * qh;
                int h = ((dh + 1) * (qh + 2));
                return h;
        }

        /** Compare two BigRationals.
        */
        public int compareTo(BigRational that)
        {
                int s = signum();
                int t = that.signum();

                if (s != t) {
                        return (s < t ? -1 : 1);
                }

                // optimization: both zero.
                if (s == 0) {
                        return 0;
                }

                // note: both m_q are positive.
                return bigIntegerMultiply(m_n, that.m_q).compareTo(
                        bigIntegerMultiply(that.m_n, m_q));
        }

        /** Compare to BigInteger.
        */
        public int compareTo(BigInteger that)
        {
                return compareTo(new BigRational(that));
        }

        /** Compare to long.
        */
        public int compareTo(long that)
        {
                return compareTo(new BigRational(that));
        }

        /** Compare object (BigRational/BigInteger/Long/Integer).
        * Implements Comparable.compareTo(Object) (jdk-1.2 and later).
        * Only BigRational/BigInteger/Long/Integer objects allowed, will throw otherwise.
        */
        public int compareTo(Object object)
        {
                if (object instanceof Integer) {
                        return compareTo(((Integer)object).longValue());
                }

                if (object instanceof Long) {
                        return compareTo(((Long)object).longValue());
                }

                if (object instanceof BigInteger) {
                        return compareTo((BigInteger)object);
                }

                // now assuming that it's either 'instanceof BigRational'
                // or it'll throw a ClassCastException.

                return compareTo((BigRational)object);
        }

        /** Convert to BigInteger, by rounding.
        */
        public BigInteger bigIntegerValue()
        {
                return round().m_n;
        }

        /** Convert to long, by rounding and delegating to BigInteger.
        * Implements Number.longValue().
        */
        public long longValue()
        {
                // delegate to BigInteger.
                return bigIntegerValue().longValue();
        }

        /** Convert to int, by rounding and delegating to BigInteger.
        * Implements Number.intValue().
        */
        public int intValue()
        {
                // delegate to BigInteger.
                return bigIntegerValue().intValue();
        }

        /** Manifactor a BigRational from a BigInteger.
        */
        public static BigRational valueOf(BigInteger value)
        {
                return new BigRational(value);
        }

        /** Manifactor a BigRational from a long fix number integer.
        */
        public static BigRational valueOf(long value)
        {
                return new BigRational(value);
        }

        // note: byte/short/int implicitly upgraded to long,
        // so we don't implement valueOf(int) et al.

        /** Rounding mode to round away from zero.
        */
        public final static int ROUND_UP = 0;

        /** Rounding mode to round towards zero.
        */
        public final static int ROUND_DOWN = 1;

        /** Rounding mode to round towards positive infinity.
        */
        public final static int ROUND_CEILING = 2;

        /** Rounding mode to round towards negative infinity.
        */
        public final static int ROUND_FLOOR = 3;

        /** Rounding mode to round towards nearest neighbor unless both
        * neighbors are equidistant, in which case to round up.
        */
        public final static int ROUND_HALF_UP = 4;

        /** Rounding mode to round towards nearest neighbor unless both
        * neighbors are equidistant, in which case to round down.
        */
        public final static int ROUND_HALF_DOWN = 5;

        /** Rounding mode to round towards the nearest neighbor unless both
        * neighbors are equidistant, in which case to round towards the even neighbor.
        */
        public final static int ROUND_HALF_EVEN = 6;

        /** Rounding mode to assert that the requested operation has an exact
        * result, hence no rounding is necessary.
        * If this rounding mode is specified on an operation that yields an inexact result,
        * an ArithmeticException is thrown.
        */
        public final static int ROUND_UNNECESSARY = 7;

        /** Rounding mode to round towards nearest neighbor unless both
        * neighbors are equidistant, in which case to round ceiling.
        */
        public final static int ROUND_HALF_CEILING = 8;

        /** Rounding mode to round towards nearest neighbor unless both
        * neighbors are equidistant, in which case to round floor.
        */
        public final static int ROUND_HALF_FLOOR = 9;

        /** Rounding mode to round towards the nearest neighbor unless both
        * neighbors are equidistant, in which case to round towards the odd neighbor.
        */
        public final static int ROUND_HALF_ODD = 10;

        /** Default round mode, ROUND_HALF_UP.
        */
        public final static int DEFAULT_ROUND_MODE = ROUND_HALF_UP;

        /** Round.
        */
        public BigRational round(int roundMode)
        {
                // return self if we don't need to round, independant of rounding mode
                if (integerp()) {
                        return this;
                }

                return new BigRational(roundToBigInteger(roundMode));
        }

        /** Round to BigInteger helper function.
        * Internally used.
        */
        private BigInteger roundToBigInteger(int roundMode)
        {
                // note: remainder and its duplicate are calculated for all cases.

                BigInteger d = m_n;
                BigInteger q = m_q;

                int sgn = d.signum();
                if (sgn == 0) {
                        return d;
                }

                // keep info on the sign
                boolean pos = (sgn > 0);

                // operate on positive values
                if (!pos) {
                        d = d.negate();
                }

                BigInteger[] divrem = d.divideAndRemainder(q);
                BigInteger dv = divrem[0];
                BigInteger r = divrem[1];

                // return if we don't need to round, independant of rounding mode
                if (bigIntegerZerop(r)) {
                        if (!pos) {
                                dv = dv.negate();
                        }

                        return dv;
                }

                boolean up = false;
                int comp = r.multiply(BIG_INTEGER_TWO).compareTo(q);

                switch (roundMode) {

                // Rounding mode to round away from zero.
                case ROUND_UP :
                        up = true;
                        break;

                // Rounding mode to round towards zero.
                case ROUND_DOWN :
                        up = false;
                        break;

                // Rounding mode to round towards positive infinity.
                case ROUND_CEILING :
                        up = pos;
                        break;

                // Rounding mode to round towards negative infinity.
                case ROUND_FLOOR :
                        up = !pos;
                        break;

                // Rounding mode to round towards "nearest neighbor" unless both
                // neighbors are equidistant, in which case round up.
                case ROUND_HALF_UP :
                        up = (comp >= 0);
                        break;

                // Rounding mode to round towards "nearest neighbor" unless both
                // neighbors are equidistant, in which case round down.
                case ROUND_HALF_DOWN :
                        up = (comp > 0);
                        break;

                case ROUND_HALF_CEILING :
                        up = (comp != 0 ? comp > 0 : pos);
                        break;

                case ROUND_HALF_FLOOR :
                        up = (comp != 0 ? comp > 0 : !pos);
                        break;

                // Rounding mode to round towards the "nearest neighbor" unless both
                // neighbors are equidistant, in which case, round towards the even neighbor.
                case ROUND_HALF_EVEN :
                        up = (comp != 0 ?
                                comp > 0 :
                                !bigIntegerZerop(dv.remainder(BIG_INTEGER_TWO)));
                        break;

                case ROUND_HALF_ODD :
                        up = (comp != 0 ?
                                comp > 0 :
                                bigIntegerZerop(dv.remainder(BIG_INTEGER_TWO)));
                        break;

                // Rounding mode to assert that the requested operation has an exact
                // result, hence no rounding is necessary.  If this rounding mode is
                // specified on an operation that yields an inexact result, an
                // ArithmeticException is thrown.
                case ROUND_UNNECESSARY :
                        if (!bigIntegerZerop(r)) {
                                throw new ArithmeticException("rounding necessary");
                        }
                        up = false;
                        break;

                default :
                        throw new IllegalArgumentException("unsupported rounding mode");
                }

                if (up) {
                        dv = dv.add(BIG_INTEGER_ONE);
                }

                if (!pos) {
                        dv = dv.negate();
                }

                return dv;
        }

        /** Round by default mode.
        */
        public BigRational round()
        {
                return round(DEFAULT_ROUND_MODE);
        }

        /** Floor, round towards negative infinity.
        */
        public BigRational floor()
        {
                return round(ROUND_FLOOR);
        }

        /** Ceiling, round towards positive infinity.
        */
        public BigRational ceiling()
        {
                return round(ROUND_CEILING);
        }

        /** An alias to ceiling().
        * [Name: see class Math.]
        */
        public BigRational ceil()
        {
                return ceiling();
        }

        /** Truncate, round towards zero.
        */
        public BigRational truncate()
        {
                return round(ROUND_DOWN);
        }

        /** An alias to truncate().
        */
        public BigRational trunc()
        {
                return truncate();
        }

        /** Integer part.
        */
        public BigRational integerPart()
        {
                return round(ROUND_DOWN);
        }

        /** Fractional part.
        */
        public BigRational fractionalPart()
        {
                BigRational ip = integerPart();
                BigRational fp = subtract(ip);

                // this==ip+fp; sign(fp)==sign(this)
                return fp;
        }

        /** Return an array of BigRationals with both integer and fractional part.
        */
        public BigRational[] integerAndFractionalPart()
        {
                // note: this duplicates fractionalPart() code, for speed.

                BigRational ip = integerPart();
                BigRational fp = subtract(ip);

                BigRational[] pp = new BigRational[2];
                pp[0] = ip;
                pp[1] = fp;

                return pp;
        }

        /** Run tests, implementation is commented out,
        * run at development and integration time.
        * Throws runtime exceptions.
        */
        public static void test()
        {
/*
**		// implementation can be commented out.
**
**		// note: don't use c style comments here,
**		// in order to be commented out altogether.
**
**		// note: just throwing RuntimeExceptions here, for convenience.
**
**		// note: only testing the _public_ interfaces.
**		// typically not testing the aliases.
**
**		// note: explicitly using 'BigRational.' prefix, where appropriate
**		// (e.g. BigRational.DEFAULT_RADIX), to be able to take these tests
**		// out of this class.
**
**		if (
**			// well, this makes sense, doesn't it?
**			BigRational.DEFAULT_RADIX != 10
**		) {
**			throw new RuntimeException("BigRational default radix");
**		}
**
**		// note: we're not using BigIntegers in these tests
**
**		// constructors
**
**		try {
**			new BigRational(2, 0);
**			throw new RuntimeException("BigRational zero quotient");
**		} catch (NumberFormatException e) {
**		}
**
**		// long/long ctor
**		if (
**			!(new BigRational(21, 35)).equals(new BigRational(3, 5)) ||
**			!(new BigRational(-21, 35)).equals(new BigRational(-3, 5)) ||
**			!(new BigRational(-21, 35)).equals(new BigRational(3, -5)) ||
**			!(new BigRational(21, -35)).equals(new BigRational(-3, 5)) ||
**			!(new BigRational(-21, -35)).equals(new BigRational(3, 5))
**		) {
**			throw new RuntimeException("BigRational equality");
**		}
**
**		// long ctor
**		if (
**			!(new BigRational(1)).equals(new BigRational(1)) ||
**			!(new BigRational(0)).equals(new BigRational(0)) ||
**			!(new BigRational(2)).equals(new BigRational(2)) ||
**			!(new BigRational(-1)).equals(new BigRational(-1))
**		) {
**			throw new RuntimeException("BigRational equality");
**		}
**
**		// string ctors
**
**		if (
**			!(new BigRational("11")).toString().equals("11") ||
**			!(new BigRational("-11")).toString().equals("-11") ||
**			!(new BigRational("+11")).toString().equals("11")
**		) {
**			throw new RuntimeException("BigRational normalization");
**		}
**
**		if (
**			!(new BigRational("21/35")).toString().equals("3/5") ||
**			!(new BigRational("-21/35")).toString().equals("-3/5") ||
**			!(new BigRational("21/-35")).toString().equals("-3/5") ||
**			// special, but defined
**			!(new BigRational("-21/-35")).toString().equals("3/5") ||
**			!(new BigRational("+21/35")).toString().equals("3/5") ||
**			// special, but defined
**			!(new BigRational("21/+35")).toString().equals("3/5") ||
**			// special, but defined
**			!(new BigRational("+21/+35")).toString().equals("3/5")
**		) {
**			throw new RuntimeException("BigRational normalization");
**		}
**
**		// all special
**		if (
**			// 1/x
**			!(new BigRational("/3")).toString().equals("1/3") ||
**			!(new BigRational("-/3")).toString().equals("-1/3") ||
**			!(new BigRational("/-3")).toString().equals("-1/3") ||
**			// x/1
**			!(new BigRational("3/")).toString().equals("3") ||
**			!(new BigRational("-3/")).toString().equals("-3") ||
**			!(new BigRational("3/-")).toString().equals("-3") ||
**			!(new BigRational("-3/-")).toString().equals("3") ||
**			// special, but defined
**			!(new BigRational("/")).toString().equals("1") ||
**			!(new BigRational("-/")).toString().equals("-1") ||
**			!(new BigRational("/-")).toString().equals("-1") ||
**			// even more special, but defined
**			!(new BigRational("-/-")).toString().equals("1")
**		) {
**			throw new RuntimeException("BigRational special formats");
**		}
**
**		if (
**			!(new BigRational("3.4")).toString().equals("17/5") ||
**			!(new BigRational("-3.4")).toString().equals("-17/5") ||
**			!(new BigRational("+3.4")).toString().equals("17/5")
**		) {
**			throw new RuntimeException("BigRational normalization");
**		}
**
**		if (
**			!(new BigRational("5.")).toString().equals("5") ||
**			!(new BigRational("-5.")).toString().equals("-5") ||
**			!(new BigRational(".7")).toString().equals("7/10") ||
**			!(new BigRational("-.7")).toString().equals("-7/10") ||
**			// special, but defined
**			!(new BigRational(".")).toString().equals("0") ||
**			!(new BigRational("-.")).toString().equals("0") ||
**			!(new BigRational("-")).toString().equals("0") ||
**			// special, but defined
**			!(new BigRational("")).toString().equals("0")
**		) {
**			throw new RuntimeException("BigRational missing leading/trailing zero");
**		}
**
**		// other radix
**		if (
**			!(new BigRational("f/37", 0x10)).toString().equals("3/11") ||
**			!(new BigRational("f.37", 0x10)).toString().equals("3895/256") ||
**			!(new BigRational("-dcba.efgh", 23)).toString().equals("-46112938320/279841") ||
**			!(new BigRational("1011101011010110", 2)).toString(0x10).equals("bad6")
**		) {
**			throw new RuntimeException("BigRational radix");
**		}
**
**		// exceptions
**
**		try {
**			new BigRational("2.5/3");
**			throw new RuntimeException("BigRational slash and dot");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2/3.5");
**			throw new RuntimeException("BigRational slash and dot");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2.5/3.5");
**			throw new RuntimeException("BigRational slash and dot");
**		} catch (NumberFormatException e) {
**		}
**
**		try {
**			new BigRational("+-2/3");
**			throw new RuntimeException("BigRational multiple signs, embedded signs");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("-+2/3");
**			throw new RuntimeException("BigRational multiple signs, embedded signs");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("++2/3");
**			throw new RuntimeException("BigRational multiple signs, embedded signs");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("--2/3");
**			throw new RuntimeException("BigRational multiple signs, embedded signs");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2-/3");
**			throw new RuntimeException("BigRational multiple signs, embedded signs");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2+/3");
**			throw new RuntimeException("BigRational multiple signs, embedded signs");
**		} catch (NumberFormatException e) {
**		}
**
**		try {
**			new BigRational("2.+3");
**			throw new RuntimeException("BigRational sign in fraction");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2.-3");
**			throw new RuntimeException("BigRational sign in fraction");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2.3+");
**			throw new RuntimeException("BigRational sign in fraction");
**		} catch (NumberFormatException e) {
**		}
**		try {
**			new BigRational("2.3-");
**			throw new RuntimeException("BigRational sign in fraction");
**		} catch (NumberFormatException e) {
**		}
**
**		// scaling
**
**		if (
**			// zero scale
**			!(new BigRational(123456, 0, BigRational.DEFAULT_RADIX)).toString().equals("123456") ||
**			!(new BigRational(123456, 1, BigRational.DEFAULT_RADIX)).toString().equals("61728/5") ||
**			!(new BigRational(123456, 2, BigRational.DEFAULT_RADIX)).toString().equals("30864/25") ||
**			!(new BigRational(123456, 5, BigRational.DEFAULT_RADIX)).toString().equals("3858/3125") ||
**			// <1
**			!(new BigRational(123456, 6, BigRational.DEFAULT_RADIX)).toString().equals("1929/15625") ||
**			// negative scale
**			!(new BigRational(123456, -1, BigRational.DEFAULT_RADIX)).toString().equals("1234560") ||
**			!(new BigRational(123456, -2, BigRational.DEFAULT_RADIX)).toString().equals("12345600")
**		) {
**			throw new RuntimeException("BigRational normalization");
**		}
**
**		if (
**			!BigRational.ZERO.toString().equals("0") ||
**			!BigRational.ONE.toString().equals("1")
**		) {
**			throw new RuntimeException("BigRational constants");
**		}
**
**		// toStringDot()
**		if (
**			!(new BigRational("1234.5678")).toStringDot(4).equals("1234.5678") ||
**			!(new BigRational("1234.5678")).toStringDot(5).equals("1234.56780") ||
**			!(new BigRational("1234.5678")).toStringDot(6).equals("1234.567800") ||
**			!(new BigRational("1234.5678")).toStringDot(3).equals("1234.568") ||
**			!(new BigRational("1234.5678")).toStringDot(2).equals("1234.57") ||
**			!(new BigRational("1234.5678")).toStringDot(1).equals("1234.6") ||
**			!(new BigRational("1234.5678")).toStringDot(0).equals("1235") ||
**			!(new BigRational("1234.5678")).toStringDot(-1).equals("1230") ||
**			!(new BigRational("1234.5678")).toStringDot(-2).equals("1200") ||
**			!(new BigRational("1234.5678")).toStringDot(-3).equals("1000") ||
**			!(new BigRational("1234.5678")).toStringDot(-4).equals("0") ||
**			!(new BigRational("1234.5678")).toStringDot(-5).equals("0") ||
**			!(new BigRational("1234.5678")).toStringDot(-6).equals("0")
**		) {
**			throw new RuntimeException("BigRational string dot");
**		}
**		if (
**			!(new BigRational("8765.4321")).toStringDot(-2).equals("8800") ||
**			!(new BigRational("0.0148")).toStringDot(6).equals("0.014800") ||
**			!(new BigRational("0.0148")).toStringDot(4).equals("0.0148") ||
**			!(new BigRational("0.0148")).toStringDot(3).equals("0.015") ||
**			!(new BigRational("0.0148")).toStringDot(2).equals("0.01") ||
**			!(new BigRational("0.0148")).toStringDot(1).equals("0.0") ||
**			!(new BigRational("0.001")).toStringDot(4).equals("0.0010") ||
**			!(new BigRational("0.001")).toStringDot(3).equals("0.001") ||
**			!(new BigRational("0.001")).toStringDot(2).equals("0.00") ||
**			!(new BigRational("0.001")).toStringDot(1).equals("0.0") ||
**			!(new BigRational("0.001")).toStringDot(0).equals("0") ||
**			!(new BigRational("0.001")).toStringDot(-1).equals("0") ||
**			!(new BigRational("0.001")).toStringDot(-2).equals("0")
**		) {
**			throw new RuntimeException("BigRational string dot");
**		}
**		if (
**			!(new BigRational("-1234.5678")).toStringDot(4).equals("-1234.5678") ||
**			!(new BigRational("-1234.5678")).toStringDot(5).equals("-1234.56780") ||
**			!(new BigRational("-1234.5678")).toStringDot(6).equals("-1234.567800") ||
**			!(new BigRational("-1234.5678")).toStringDot(3).equals("-1234.568") ||
**			!(new BigRational("-1234.5678")).toStringDot(2).equals("-1234.57") ||
**			!(new BigRational("-1234.5678")).toStringDot(1).equals("-1234.6") ||
**			!(new BigRational("-1234.5678")).toStringDot(0).equals("-1235") ||
**			!(new BigRational("-1234.5678")).toStringDot(-1).equals("-1230") ||
**			!(new BigRational("-1234.5678")).toStringDot(-2).equals("-1200") ||
**			!(new BigRational("-1234.5678")).toStringDot(-3).equals("-1000") ||
**			!(new BigRational("-1234.5678")).toStringDot(-4).equals("0") ||
**			!(new BigRational("-1234.5678")).toStringDot(-5).equals("0")
**		) {
**			throw new RuntimeException("BigRational string dot");
**		}
**		// other radix
**		if (
**			!(new BigRational("1234.5678", 20)).toStringDot(3, 20).equals("1234.567") ||
**			!(new BigRational("abcd.5b7g", 20)).toStringDot(-2, 20).equals("ac00")
**		) {
**			throw new RuntimeException("BigRational string dot radix");
**		}
**
**		// note: some of the arithmetic long forms not tested as well.
**
**		// add()
**		if (
**			!(new BigRational(3, 5)).add(new BigRational(7, 11)).toString().equals("68/55") ||
**			!(new BigRational(3, 5)).add(new BigRational(-7, 11)).toString().equals("-2/55") ||
**			!(new BigRational(-3, 5)).add(new BigRational(7, 11)).toString().equals("2/55") ||
**			!(new BigRational(-3, 5)).add(new BigRational(-7, 11)).toString().equals("-68/55") ||
**			// same denominator
**			!(new BigRational(3, 5)).add(new BigRational(1, 5)).toString().equals("4/5") ||
**			// with integers
**			!(new BigRational(3, 5)).add(new BigRational(1)).toString().equals("8/5") ||
**			!(new BigRational(2)).add(new BigRational(3, 5)).toString().equals("13/5") ||
**			// zero
**			!(new BigRational(3, 5)).add(BigRational.ZERO).toString().equals("3/5") ||
**			!BigRational.ZERO.add(new BigRational(3, 5)).toString().equals("3/5")
**		) {
**			throw new RuntimeException("BigRational add");
**		}
**
**		// subtract()
**		if (
**			!(new BigRational(3, 5)).subtract(new BigRational(7, 11)).toString().equals("-2/55") ||
**			!(new BigRational(3, 5)).subtract(new BigRational(-7, 11)).toString().equals("68/55") ||
**			!(new BigRational(-3, 5)).subtract(new BigRational(7, 11)).toString().equals("-68/55") ||
**			!(new BigRational(-3, 5)).subtract(new BigRational(-7, 11)).toString().equals("2/55") ||
**			// same denominator
**			!(new BigRational(3, 5)).subtract(new BigRational(1, 5)).toString().equals("2/5") ||
**			// with integers
**			!(new BigRational(3, 5)).subtract(new BigRational(1)).toString().equals("-2/5") ||
**			!(new BigRational(2)).subtract(new BigRational(3, 5)).toString().equals("7/5") ||
**			// zero
**			!(new BigRational(3, 5)).subtract(BigRational.ZERO).toString().equals("3/5") ||
**			!BigRational.ZERO.subtract(new BigRational(3, 5)).toString().equals("-3/5")
**		) {
**			throw new RuntimeException("BigRational subtract");
**		}
**
**		// normalization/proxying, e.g after subtract
**		if (
**			(new BigRational(7, 5)).subtract(new BigRational(2, 5)).compareTo(1) != 0 ||
**			(new BigRational(7, 5)).subtract(new BigRational(7, 5)).compareTo(0) != 0 ||
**			(new BigRational(7, 5)).subtract(new BigRational(12, 5)).compareTo(-1) != 0
**		) {
**			throw new RuntimeException("BigRational normalization/proxying");
**		}
**
**		// multiply()
**		if (
**			!(new BigRational(3, 5)).multiply(new BigRational(7, 11)).toString().equals("21/55") ||
**			!(new BigRational(3, 5)).multiply(new BigRational(-7, 11)).toString().equals("-21/55") ||
**			!(new BigRational(-3, 5)).multiply(new BigRational(7, 11)).toString().equals("-21/55") ||
**			!(new BigRational(-3, 5)).multiply(new BigRational(-7, 11)).toString().equals("21/55") ||
**			!(new BigRational(3, 5)).multiply(7).toString().equals("21/5") ||
**			!(new BigRational(-3, 5)).multiply(7).toString().equals("-21/5") ||
**			!(new BigRational(3, 5)).multiply(-7).toString().equals("-21/5") ||
**			!(new BigRational(-3, 5)).multiply(-7).toString().equals("21/5")
**		) {
**			throw new RuntimeException("BigRational multiply");
**		}
**		// multiply() with integers, 0, etc. (some repetitions too)
**		if (
**			!(new BigRational(3, 5)).multiply(new BigRational(7, 1)).toString().equals("21/5") ||
**			!(new BigRational(3, 5)).multiply(new BigRational(1, 7)).toString().equals("3/35") ||
**			!(new BigRational(3, 1)).multiply(new BigRational(7, 11)).toString().equals("21/11") ||
**			!(new BigRational(3, 5)).multiply(new BigRational(0)).toString().equals("0") ||
**			!(new BigRational(0)).multiply(new BigRational(3, 5)).toString().equals("0") ||
**			!(new BigRational(3, 5)).multiply(new BigRational(1)).toString().equals("3/5") ||
**			!(new BigRational(3, 5)).multiply(new BigRational(-1)).toString().equals("-3/5")
**		) {
**			throw new RuntimeException("BigRational multiply");
**		}
**
**		// divide()
**		if (
**			!(new BigRational(3, 5)).divide(new BigRational(7, 11)).toString().equals("33/35") ||
**			!(new BigRational(3, 5)).divide(new BigRational(-7, 11)).toString().equals("-33/35") ||
**			!(new BigRational(-3, 5)).divide(new BigRational(7, 11)).toString().equals("-33/35") ||
**			!(new BigRational(-3, 5)).divide(new BigRational(-7, 11)).toString().equals("33/35") ||
**			!(new BigRational(3, 5)).divide(7).toString().equals("3/35") ||
**			!(new BigRational(-3, 5)).divide(7).toString().equals("-3/35") ||
**			!(new BigRational(3, 5)).divide(-7).toString().equals("-3/35") ||
**			!(new BigRational(-3, 5)).divide(-7).toString().equals("3/35")
**		) {
**			throw new RuntimeException("BigRational divide");
**		}
**
**		try {
**			(new BigRational(3, 5)).divide(new BigRational(0));
**			throw new RuntimeException("BigRational divide");
**		} catch (ArithmeticException e) {
**		}
**		try {
**			(new BigRational(-3, 5)).divide(new BigRational(0));
**			throw new RuntimeException("BigRational divide");
**		} catch (ArithmeticException e) {
**		}
**		try {
**			(new BigRational(3, 5)).divide(0);
**			throw new RuntimeException("BigRational divide");
**		} catch (ArithmeticException e) {
**		}
**		try {
**			(new BigRational(-3, 5)).divide(0);
**			throw new RuntimeException("BigRational divide");
**		} catch (ArithmeticException e) {
**		}
**
**		// power()
**		if (
**			!(new BigRational(3, 5)).power(7).toString().equals("2187/78125") ||
**			!(new BigRational(-3, 5)).power(7).toString().equals("-2187/78125") ||
**			!(new BigRational(3, 5)).power(-7).toString().equals("78125/2187") ||
**			!(new BigRational(-3, 5)).power(-7).toString().equals("-78125/2187") ||
**			!(new BigRational(3, 5)).power(6).toString().equals("729/15625") ||
**			!(new BigRational(-3, 5)).power(6).toString().equals("729/15625") ||
**			!(new BigRational(3, 5)).power(0).toString().equals("1") ||
**			!(new BigRational(-3, 5)).power(0).toString().equals("1") ||
**			!(new BigRational(0)).power(1).toString().equals("0") ||
**			!(new BigRational(1)).power(0).toString().equals("1")
**		) {
**			throw new RuntimeException("BigRational power");
**		}
**		if (
**			!(new BigRational(3, 5)).power(0).equals(BigRational.ONE) ||
**			!(new BigRational(3, 5)).power(1).equals(new BigRational(3, 5)) ||
**			!(new BigRational(3, 5)).power(-1).equals((new BigRational(3, 5)).invert())
**		) {
**			throw new RuntimeException("BigRational more power");
**		}
**
**		try {
**			(new BigRational(0)).power(0);
**			throw new RuntimeException("BigRational zeroth power of zero");
**		} catch (ArithmeticException e) {
**		}
**
**		// remainder()
**		if (
**			!(new BigRational(5)).remainder(new BigRational(3)).equals(new BigRational(2)) ||
**			!(new BigRational(-5)).remainder(new BigRational(3)).equals(new BigRational(-2)) ||
**			!(new BigRational(5)).remainder(new BigRational(-3)).equals(new BigRational(2)) ||
**			!(new BigRational(-5)).remainder(new BigRational(-3)).equals(new BigRational(-2)) ||
**			!(new BigRational(0)).remainder(new BigRational(1)).equals(new BigRational(0)) ||
**			!(new BigRational("5.6")).remainder(new BigRational("1.8")).equals(new BigRational(1, 5)) ||
**			!(new BigRational("-5.6")).remainder(new BigRational("1.8")).equals(new BigRational(-1, 5)) ||
**			!(new BigRational("5.6")).remainder(new BigRational("-1.8")).equals(new BigRational(1, 5)) ||
**			!(new BigRational("-5.6")).remainder(new BigRational("-1.8")).equals(new BigRational(-1, 5)) ||
**			!(new BigRational("1")).remainder(new BigRational("0.13")).equals(new BigRational("0.09"))
**		) {
**			throw new RuntimeException("BigRational remainder");
**		}
**
**		// modulus()
**		if (
**			!(new BigRational(5)).modulus(new BigRational(3)).equals(new BigRational(2)) ||
**			!(new BigRational(-5)).modulus(new BigRational(3)).equals(new BigRational(1)) ||
**			!(new BigRational(5)).modulus(new BigRational(-3)).equals(new BigRational(-1)) ||
**			!(new BigRational(-5)).modulus(new BigRational(-3)).equals(new BigRational(-2)) ||
**			!(new BigRational(0)).modulus(new BigRational(1)).equals(new BigRational(0)) ||
**			!(new BigRational("5.6")).modulus(new BigRational("1.8")).equals(new BigRational(1, 5)) ||
**			!(new BigRational("-5.6")).modulus(new BigRational("1.8")).equals(new BigRational(8, 5)) ||
**			!(new BigRational("5.6")).modulus(new BigRational("-1.8")).equals(new BigRational(-8, 5)) ||
**			!(new BigRational("-5.6")).modulus(new BigRational("-1.8")).equals(new BigRational(-1, 5)) ||
**			!(new BigRational("1")).modulus(new BigRational("0.13")).equals(new BigRational("0.09"))
**		) {
**			throw new RuntimeException("BigRational modulus");
**		}
**
**		// signum()
**		if (
**			(new BigRational(0)).signum() != 0 ||
**			(new BigRational(1)).signum() != 1 ||
**			(new BigRational(-1)).signum() != -1 ||
**			(new BigRational(2)).signum() != 1 ||
**			(new BigRational(-2)).signum() != -1 ||
**			(new BigRational(3, 5)).signum() != 1 ||
**			(new BigRational(-3, 5)).signum() != -1
**		) {
**			throw new RuntimeException("BigRational signum");
**		}
**
**		// absolute()
**		if (
**			!(new BigRational(0)).absolute().toString().equals("0") ||
**			!(new BigRational(3, 5)).absolute().toString().equals("3/5") ||
**			!(new BigRational(-3, 5)).absolute().toString().equals("3/5")
**		) {
**			throw new RuntimeException("BigRational absolute");
**		}
**
**		// negate()
**		if (
**			!(new BigRational(0)).negate().toString().equals("0") ||
**			!(new BigRational(1)).negate().toString().equals("-1") ||
**			!(new BigRational(-1)).negate().toString().equals("1") ||
**			!(new BigRational(3, 5)).negate().toString().equals("-3/5") ||
**			!(new BigRational(-3, 5)).negate().toString().equals("3/5")
**		) {
**			throw new RuntimeException("BigRational negate");
**		}
**
**		// invert()
**		if (
**			!(new BigRational(3, 5)).invert().toString().equals("5/3") ||
**			!(new BigRational(-3, 5)).invert().toString().equals("-5/3") ||
**			!(new BigRational(11, 7)).invert().toString().equals("7/11") ||
**			!(new BigRational(-11, 7)).invert().toString().equals("-7/11") ||
**			!(new BigRational(1)).invert().toString().equals("1") ||
**			!(new BigRational(-1)).invert().toString().equals("-1") ||
**			!(new BigRational(2)).invert().toString().equals("1/2")
**		) {
**			throw new RuntimeException("BigRational invert");
**		}
**
**		// minimum()
**		if (
**			!(new BigRational(3, 5)).minimum(new BigRational(7, 11)).toString().equals("3/5") ||
**			!(new BigRational(-3, 5)).minimum(new BigRational(7, 11)).toString().equals("-3/5") ||
**			!(new BigRational(3, 5)).minimum(new BigRational(-7, 11)).toString().equals("-7/11") ||
**			!(new BigRational(-3, 5)).minimum(new BigRational(-7, 11)).toString().equals("-7/11") ||
**			!(new BigRational(7, 11)).minimum(new BigRational(3, 5)).toString().equals("3/5")
**		) {
**			throw new RuntimeException("BigRational minimum");
**		}
**
**		// maximum()
**		if (
**			!(new BigRational(3, 5)).maximum(new BigRational(7, 11)).toString().equals("7/11") ||
**			!(new BigRational(-3, 5)).maximum(new BigRational(7, 11)).toString().equals("7/11") ||
**			!(new BigRational(3, 5)).maximum(new BigRational(-7, 11)).toString().equals("3/5") ||
**			!(new BigRational(-3, 5)).maximum(new BigRational(-7, 11)).toString().equals("-3/5") ||
**			!(new BigRational(7, 11)).maximum(new BigRational(3, 5)).toString().equals("7/11")
**		) {
**			throw new RuntimeException("BigRational maximum");
**		}
**
**		// equals()
**
**		if (
**			!BigRational.ONE.equals(BigRational.ONE) ||
**			!BigRational.ONE.equals(new BigRational(3, 3)) ||
**			!(new BigRational(3, 5)).equals(new BigRational(3, 5)) ||
**			(new BigRational(3, 5)).equals(new BigRational(5, 3))
**		) {
**			throw new RuntimeException("BigRational equals");
**		}
**
**		if (
**			!BigRational.ONE.equals((Object)BigRational.ONE) ||
**			!BigRational.ONE.equals((Object)new BigRational(3, 3)) ||
**			!(new BigRational(3, 5)).equals((Object)new BigRational(3, 5)) ||
**			(new BigRational(3, 5)).equals((Object)new BigRational(5, 3))
**		) {
**			throw new RuntimeException("BigRational polymorph equals");
**		}
**
**		// hashCode()
**		if (
**			(new BigRational(3, 5)).hashCode() != (new BigRational(6, 10)).hashCode()
**		) {
**			throw new RuntimeException("BigRational hash code");
**		}
**		// well, the necessity of these test ain't clear.
**		if (
**			BigRational.ONE.hashCode() == BigRational.ZERO.hashCode() ||
**			(new BigRational(3, 5)).hashCode() == (new BigRational(5, 3)).hashCode() ||
**			(new BigRational(3, 5)).hashCode() == (new BigRational(-3, 5)).hashCode() ||
**			(new BigRational(4)).hashCode() == (new BigRational(8)).hashCode()
**		) {
**			// [commented out since not a misbehavior or error.]
**			// throw new RuntimeException("BigRational hash code");
**		}
**
**		// compareTo()
**		if (
**			(new BigRational(3, 5)).compareTo(new BigRational(3, 5)) != 0 ||
**			(new BigRational(3, 5)).compareTo(new BigRational(5, 3)) != -1 ||
**			(new BigRational(5, 3)).compareTo(new BigRational(3, 5)) != 1 ||
**			(new BigRational(3, 5)).compareTo(new BigRational(-5, 3)) != 1 ||
**			(new BigRational(-5, 3)).compareTo(new BigRational(3, 5)) != -1
**		) {
**			throw new RuntimeException("BigRational compare to");
**		}
**
**		if (
**			(new BigRational(3, 5)).compareTo((Object)new BigRational(3, 5)) != 0 ||
**			(new BigRational(3, 5)).compareTo((Object)new BigRational(5, 3)) != -1 ||
**			(new BigRational(5, 3)).compareTo((Object)new BigRational(3, 5)) != 1 ||
**			(new BigRational(3, 5)).compareTo((Object)new BigRational(-5, 3)) != 1 ||
**			(new BigRational(-5, 3)).compareTo((Object)new BigRational(3, 5)) != -1
**		) {
**			throw new RuntimeException("BigRational polymorph compare to");
**		}
**
**		if (
**			(new BigRational(3, 5)).compareTo(0) == 0 ||
**			(new BigRational(0)).compareTo(0) != 0 ||
**			(new BigRational(1)).compareTo(1) != 0 ||
**			(new BigRational(2)).compareTo(2) != 0
**		) {
**			throw new RuntimeException("BigRational small type compare to");
**		}
**
**		// longValue()/intValue()
**		if (
**			(new BigRational(7)).longValue() != 7 ||
**			(new BigRational(-7)).intValue() != -7
**		) {
**			throw new RuntimeException("BigRational long/int value");
**		}
**
**		// round()/floor()/ceiling()
**
**		if (
**			!(new BigRational("23.49")).round(BigRational.ROUND_UP).toString().equals("24") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_DOWN).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_CEILING).toString().equals("24") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_FLOOR).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_HALF_UP).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_HALF_DOWN).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_HALF_CEILING).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_HALF_EVEN).toString().equals("23") ||
**			!(new BigRational("23.49")).round(BigRational.ROUND_HALF_ODD).toString().equals("23")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**		if (
**			!(new BigRational("-23.49")).round(BigRational.ROUND_UP).toString().equals("-24") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_DOWN).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_CEILING).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_FLOOR).toString().equals("-24") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_HALF_UP).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_HALF_DOWN).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_HALF_CEILING).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_HALF_EVEN).toString().equals("-23") ||
**			!(new BigRational("-23.49")).round(BigRational.ROUND_HALF_ODD).toString().equals("-23")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**		if (
**			!(new BigRational("23.51")).round(BigRational.ROUND_UP).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_DOWN).toString().equals("23") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_CEILING).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_FLOOR).toString().equals("23") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_HALF_UP).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_HALF_DOWN).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_HALF_CEILING).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_HALF_EVEN).toString().equals("24") ||
**			!(new BigRational("23.51")).round(BigRational.ROUND_HALF_ODD).toString().equals("24")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**		if (
**			!(new BigRational("-23.51")).round(BigRational.ROUND_UP).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_DOWN).toString().equals("-23") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_CEILING).toString().equals("-23") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_FLOOR).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_HALF_UP).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_HALF_DOWN).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_HALF_CEILING).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_HALF_EVEN).toString().equals("-24") ||
**			!(new BigRational("-23.51")).round(BigRational.ROUND_HALF_ODD).toString().equals("-24")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**		if (
**			!(new BigRational("23.5")).round(BigRational.ROUND_UP).toString().equals("24") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_DOWN).toString().equals("23") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_CEILING).toString().equals("24") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_FLOOR).toString().equals("23") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_HALF_UP).toString().equals("24") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_HALF_DOWN).toString().equals("23") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_HALF_CEILING).toString().equals("24") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("23") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_HALF_EVEN).toString().equals("24") ||
**			!(new BigRational("23.5")).round(BigRational.ROUND_HALF_ODD).toString().equals("23")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**		if (
**			!(new BigRational("-23.5")).round(BigRational.ROUND_UP).toString().equals("-24") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_DOWN).toString().equals("-23") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_CEILING).toString().equals("-23") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_FLOOR).toString().equals("-24") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_HALF_UP).toString().equals("-24") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_HALF_DOWN).toString().equals("-23") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_HALF_CEILING).toString().equals("-23") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("-24") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_HALF_EVEN).toString().equals("-24") ||
**			!(new BigRational("-23.5")).round(BigRational.ROUND_HALF_ODD).toString().equals("-23")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**		if (
**			!(new BigRational("22")).round(BigRational.ROUND_UP).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_DOWN).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_CEILING).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_FLOOR).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_HALF_UP).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_HALF_DOWN).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_HALF_CEILING).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_HALF_FLOOR).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_HALF_EVEN).toString().equals("22") ||
**			!(new BigRational("22")).round(BigRational.ROUND_HALF_ODD).toString().equals("22")
**		) {
**			throw new RuntimeException("BigRational round");
**		}
**
**		if (
**			!(new BigRational("23")).round(BigRational.ROUND_UNNECESSARY).toString().equals("23") ||
**			!(new BigRational("-23")).round(BigRational.ROUND_UNNECESSARY).toString().equals("-23")
**		) {
**			throw new RuntimeException("BigRational round unnecessary");
**		}
**
**		try {
**			(new BigRational("23.5")).round(BigRational.ROUND_UNNECESSARY);
**			throw new RuntimeException("BigRational round unnecessary");
**		} catch (ArithmeticException e) {
**		}
**		try {
**			(new BigRational("-23.5")).round(BigRational.ROUND_UNNECESSARY);
**			throw new RuntimeException("BigRational round unnecessary");
**		} catch (ArithmeticException e) {
**		}
**
**		if (
**			!(new BigRational("56.8")).integerPart().toString().equals("56") ||
**			!(new BigRational("-56.8")).integerPart().toString().equals("-56") ||
**			!(new BigRational("0.8")).integerPart().toString().equals("0") ||
**			!(new BigRational("-0.8")).integerPart().toString().equals("0")
**		) {
**			throw new RuntimeException("BigRational integer part");
**		}
**
**		if (
**			!(new BigRational("56.8")).fractionalPart().equals(new BigRational("0.8")) ||
**			!(new BigRational("-56.8")).fractionalPart().equals(new BigRational("-0.8")) ||
**			!(new BigRational("0.8")).fractionalPart().equals(new BigRational("0.8")) ||
**			!(new BigRational("-0.8")).fractionalPart().equals(new BigRational("-0.8"))
**		) {
**			throw new RuntimeException("BigRational fractional part");
**		}
**
**		if (
**			!(new BigRational("56.8")).integerAndFractionalPart()[0].equals(new BigRational("56")) ||
**			!(new BigRational("56.8")).integerAndFractionalPart()[1].equals(new BigRational("0.8")) ||
**			!(new BigRational("-56.8")).integerAndFractionalPart()[0].equals(new BigRational("-56")) ||
**			!(new BigRational("-56.8")).integerAndFractionalPart()[1].equals(new BigRational("-0.8"))
**		) {
**			throw new RuntimeException("BigRational integer and fractional part");
**		}
**
**		// done.
*/
        }
}
