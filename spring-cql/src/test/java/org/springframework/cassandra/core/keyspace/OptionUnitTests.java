/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cassandra.core.keyspace;

import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

/**
 * Unit tests for {@link Option}.
 *
 * @author Matthew T. Adams
 * @author JohnMcPeek
 */
public class OptionUnitTests {

	@Test(expected = IllegalArgumentException.class)
	public void testOptionWithNullName() {
		new DefaultOption(null, Object.class, true, true, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOptionWithEmptyName() {
		new DefaultOption("", Object.class, true, true, true);
	}

	@Test
	public void testOptionWithNullType() {
		new DefaultOption("opt", null, true, true, true);
		new DefaultOption("opt", null, false, true, true);
	}

	@Test
	public void testOptionWithNullTypeIsCoerceable() {
		Option op = new DefaultOption("opt", null, true, true, true);
		assertThat(op.isCoerceable("")).isTrue();
		assertThat(op.isCoerceable(null)).isTrue();
	}

	@Test
	public void testOptionValueCoercion() {
		String name = "my_option";
		Class<?> type = String.class;
		boolean requires = true;
		boolean escapes = true;
		boolean quotes = true;

		Option op = new DefaultOption(name, type, requires, escapes, quotes);

		assertThat(op.isCoerceable("opt")).isTrue();
		assertThat(op.toString("opt")).isEqualTo("'opt'");
		assertThat(op.toString("opt'n")).isEqualTo("'opt''n'");

		type = Long.class;
		escapes = false;
		quotes = false;
		op = new DefaultOption(name, type, requires, escapes, quotes);

		String expected = "1";
		for (Object value : new Object[] { 1, "1" }) {
			assertThat(op.isCoerceable(value)).isTrue();
			assertThat(op.toString(value)).isEqualTo(expected);
		}
		assertThat(op.isCoerceable("x")).isFalse();
		assertThat(op.isCoerceable(null)).isTrue();

		type = Long.class;
		escapes = false;
		quotes = true;
		op = new DefaultOption(name, type, requires, escapes, quotes);

		expected = "'1'";
		for (Object value : new Object[] { 1, "1" }) {
			assertThat(op.isCoerceable(value)).isTrue();
			assertThat(op.toString(value)).isEqualTo(expected);
		}
		assertThat(op.isCoerceable("x")).isFalse();
		assertThat(op.isCoerceable(null)).isTrue();

		type = Double.class;
		escapes = false;
		quotes = false;
		op = new DefaultOption(name, type, requires, escapes, quotes);

		String[] expecteds = new String[] { "1", "1.0", "1.0", "1", "1.0", null };
		Object[] values = new Object[] { 1, 1.0F, 1.0D, "1", "1.0", null };
		for (int i = 0; i < values.length; i++) {
			assertThat(op.isCoerceable(values[i])).isTrue();
			assertThat(op.toString(values[i])).isEqualTo(expecteds[i]);
		}
		assertThat(op.isCoerceable("x")).isFalse();
		assertThat(op.isCoerceable(null)).isTrue();

		type = RetentionPolicy.class;
		escapes = false;
		quotes = false;
		op = new DefaultOption(name, type, requires, escapes, quotes);

		assertThat(op.isCoerceable(null)).isTrue();
		assertThat(op.isCoerceable(RetentionPolicy.CLASS)).isTrue();
		assertThat(op.isCoerceable("CLASS")).isTrue();
		assertThat(op.isCoerceable("x")).isFalse();
		assertThat(op.toString("CLASS")).isEqualTo("CLASS");
		assertThat(op.toString(RetentionPolicy.CLASS)).isEqualTo("CLASS");
	}
}
