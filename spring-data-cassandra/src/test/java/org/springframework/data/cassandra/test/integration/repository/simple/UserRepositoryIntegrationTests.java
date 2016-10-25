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
package org.springframework.data.cassandra.test.integration.repository.simple;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.data.cassandra.core.CassandraOperations;

import com.google.common.collect.Lists;

/**
 * Tests for {@link UserRepository}.
 *
 * @author Alex Shvid
 * @author Matthew T. Adams
 * @author David Webb
 * @author Mark Paluch
 */
public class UserRepositoryIntegrationTests {

	UserRepository repository;

	CassandraOperations template;

	User tom, bob, alice, scott;

	List<User> all;

	public UserRepositoryIntegrationTests(UserRepository repository, CassandraOperations template) {
		this.repository = repository;
		this.template = template;
	}

	public void setUp() {

		repository.deleteAll();

		tom = new User();
		tom.setUsername("tom");
		tom.setFirstName("Tom");
		tom.setLastName("Ron");
		tom.setPassword("123");
		tom.setPlace("SF");

		bob = new User();
		bob.setUsername("bob");
		bob.setFirstName("Bob");
		bob.setLastName("White");
		bob.setPassword("555");
		bob.setPlace("NY");

		alice = new User();
		alice.setUsername("alice");
		alice.setFirstName("Alice");
		alice.setLastName("Red");
		alice.setPassword("777");
		alice.setPlace("LA");

		scott = new User();
		scott.setUsername("scott");
		scott.setFirstName("Scott");
		scott.setLastName("Van");
		scott.setPassword("444");
		scott.setPlace("Boston");

		all = Arrays.asList(tom, bob, alice, scott);
		template.batchOps().insert(all).execute();
	}

	public void before() {
		repository.deleteAll();
		setUp();
	}

	public void findByNamedQuery() {
		String name = repository.findByNamedQuery("bob");
		assertThat(name).isNotNull();
		assertThat(name).isEqualTo("Bob");
	}

	public void findsUserById() throws Exception {

		User user = repository.findOne(bob.getUsername());
		assertThat(user).isNotNull();
		assertEquals(bob, user);

	}

	public void findsAll() throws Exception {
		List<User> result = Lists.newArrayList(repository.findAll());
		assertThat(result).hasSize(all.size());
		assertThat(result.containsAll(all)).isTrue();

	}

	public void findsAllWithGivenIds() {

		Iterable<User> result = repository.findAll(Arrays.asList(bob.getUsername(), tom.getUsername()));
		assertThat(result).contains(bob, tom);
		assertThat(result).doesNotContain(alice, scott);
	}

	public void deletesUserCorrectly() throws Exception {

		repository.delete(tom);

		List<User> result = Lists.newArrayList(repository.findAll());

		assertThat(result).hasSize(all.size() - 1);
		assertThat(result).doesNotContain(tom);
	}

	public void deletesUserByIdCorrectly() {

		repository.delete(tom.getUsername());

		List<User> result = Lists.newArrayList(repository.findAll());

		assertThat(result).hasSize(all.size() - 1);
		assertThat(result).doesNotContain(tom);
	}

	public void exists() {

		String id = "tom";

		assertThat(repository.exists(id)).isTrue();

		repository.delete(id);

		assertThat(!repository.exists(id)).isTrue();

	}

	/**
	 * @see <a href="https://jira.spring.io/browse/DATACASS-182">DATACASS-182</a>
	 */
	public void save() {

		tom.setPassword(null);
		tom.setFriends(Collections.<String> emptySet());

		repository.save(tom);

		User loadedTom = repository.findOne(tom.getUsername());

		assertThat(loadedTom.getPassword()).isNull();
		assertThat(loadedTom.getFriends()).isNull();
	}

	private static void assertEquals(User user1, User user2) {
		assertThat(user2.getUsername()).isEqualTo(user1.getUsername());
		assertThat(user2.getFirstName()).isEqualTo(user1.getFirstName());
		assertThat(user2.getLastName()).isEqualTo(user1.getLastName());
		assertThat(user2.getPlace()).isEqualTo(user1.getPlace());
		assertThat(user2.getPassword()).isEqualTo(user1.getPassword());
	}
}
