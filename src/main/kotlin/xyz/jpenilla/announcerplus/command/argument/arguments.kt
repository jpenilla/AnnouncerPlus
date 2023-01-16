/*
 * This file is part of AnnouncerPlus, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.announcerplus.command.argument

import cloud.commandframework.arguments.standard.EnumArgument
import cloud.commandframework.arguments.standard.IntegerArgument
import xyz.jpenilla.announcerplus.command.Commander

private fun <C> integerArgumentBuilder(
  name: String,
  builder: IntegerArgument.Builder<C>.() -> Unit
): IntegerArgument.Builder<C> =
  IntegerArgument.builder<C>(name).apply(builder)

fun positiveInteger(name: String) = integer(name, min = 1)

fun integer(
  name: String,
  min: Int = Int.MIN_VALUE,
  max: Int = Int.MAX_VALUE
): IntegerArgument.Builder<Commander> =
  integerArgumentBuilder(name) {
    if (min != Int.MIN_VALUE) withMin(min)
    if (max != Int.MAX_VALUE) withMax(max)
  }

inline fun <reified E : Enum<E>> enum(name: String): EnumArgument.Builder<Commander, E> =
  EnumArgument.builder(E::class.java, name)
