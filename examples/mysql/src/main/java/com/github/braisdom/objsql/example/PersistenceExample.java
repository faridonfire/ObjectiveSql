package com.github.braisdom.objsql.example;

import com.github.braisdom.objsql.Databases;
import com.github.braisdom.objsql.Validator;
import com.github.braisdom.objsql.example.domains.Member;
import com.github.braisdom.objsql.example.domains.Order;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PersistenceExample extends MySQLExample {

    @Test
    public void createSimpleMemberSkipValidation() throws SQLException {
        Member newMember = new Member();
        newMember.setNo("100000")
                .setName("Pamela")
                .setGender(1)
                .setRegisteredAtWithJoda(DateTime.now())
                .setUpdatedAt(Timestamp.valueOf("2020-10-05 00:00:00"))
                .setMobile("15011112222");

        Member member = Member.create(newMember, true);

        Assert.assertNotNull(member);
        Assert.assertTrue(Member.query("no = '100000'") != null);
    }

    @Test
    public void validateMemberWithError() {
        Member newMember = new Member()
                .setNo("100")
                .setName("Pamela")
                .setGender(1)
                .setMobile("15011112222");

        Validator.Violation[] violations = newMember.validate();
        Assert.assertTrue(violations.length > 0);
    }

    @Test
    public void createSimpleMemberWithValidation() throws SQLException {
        Member newMember = new Member()
                .setNo("100000")
                .setName("Pamela")
                .setGender(1)
                .setMobile("15011112222");
        Member.create(newMember, false);
    }

    @Test
    public void createSimpleMemberCopyFromMap() throws SQLException {
        Map<String, Object> extendedAttributes = new HashMap<>();
        extendedAttributes.put("hobbies", new String[]{"Play football"});
        extendedAttributes.put("age", 28);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 10L);
        attributes.put("no", "200000");
        attributes.put("name", "Carter");
        attributes.put("gender", 1);
        attributes.put("mobile", "15011112222");
        attributes.put("extendedAttributes", extendedAttributes);

        Member member = Member.create(Member.newInstanceFrom(attributes, false),
                false);
        Assert.assertTrue(member.getId() != null);
    }

    @Test
    public void createSimpleMemberCopyFromUnderlineMap() throws SQLException {
        Map<String, Object> extendedAttributes = new HashMap<>();
        extendedAttributes.put("hobbies", new String[]{"Play football"});
        extendedAttributes.put("age", 28);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 9L);
        attributes.put("no", "200000");
        attributes.put("name", "Barbara\t");
        attributes.put("gender", 1);
        attributes.put("mobile", "15011112222");
        attributes.put("extended_attributes", extendedAttributes);

        Member member = Member.newInstanceFrom(attributes, true);
        Member.create(member, false);

        Assert.assertTrue(Member.queryFirst("no = ?", "200000") != null);
    }

    @Test
    public void createSimpleFromJsonMember() throws SQLException {
        String json = "{\"id\":7,\"no\":\"200000\",\"name\":\"Smith\",\"gender\":1,\"mobile\":\"15011112222\"," +
                "\"extendedAttributes\":{\"hobbies\":[\"Play football\"],\"age\":28}}";
        Member newMember = new GsonBuilder().create().fromJson(json, Member.class);

        Member.create(newMember, false);
    }

    @Test
    public void createMemberWithArray() throws SQLException {
        Member newMember1 = new Member()
                .setNo("200001")
                .setName("Alice")
                .setGender(0)
                .setMobile("15011112222");

        Member newMember2 = new Member()
                .setNo("200003")
                .setName("Mary")
                .setGender(0)
                .setMobile("15011112222");

        Member newMember3 = new Member()
                .setNo("200004")
                .setName("Denise")
                .setGender(0)
                .setMobile("15011112222");

        Member[] members = new Member[]{newMember1, newMember2, newMember3};
        Member.create(members, false);
    }

    @Test
    public void updateMemberById() throws SQLException {
        createMemberWithArray();

        Map<String, Object> extendedAttributes = new HashMap<>();
        extendedAttributes.put("hobbies", new String[]{"Play football", "Cooking"});
        extendedAttributes.put("age", 28);

        Member newMember = new Member()
                .setName("Smith => Jackson")
                .setExtendedAttributes(extendedAttributes);

        Member.update(1L, newMember, true);

        Member member = Member.queryByPrimaryKey(1L);
        Assert.assertTrue(member.getName().equals("Smith => Jackson"));
        Assert.assertTrue(member.getName() != null);
        Assert.assertTrue(member.getMobile() != null);
    }

    @Test
    public void updateMemberBySQL() throws SQLException {
        createMemberWithArray();

        String oldName = "Alice";
        String newName = "Smith => Jackson";
        Member.update("name = ?", "name = ?", newName, oldName);

        Member member = Member.queryByPrimaryKey(1L);
        Assert.assertTrue(member.getName().equals("Smith => Jackson"));
    }

    @Test
    public void deleteMemberById() throws SQLException {
        createMemberWithArray();

        Member.destroy(1L);

        long count = Member.countAll();
        Assert.assertTrue(count == 2);
    }

    @Test
    public void deleteMemberByPredicate() throws SQLException {
        createMemberWithArray();

        String name = "Mary";
        Member.destroy("name = ?", name);

        Assert.assertTrue(Member.countAll() == 2);
        Assert.assertTrue(Member.queryFirst("name = 'Mary'") == null);
    }

    @Test
    public void executeDeleteDenise() throws SQLException {
        createMemberWithArray();

        Member.execute(String.format("DELETE FROM %s WHERE name = 'Mary'", Member.TABLE_NAME));
        long count = Member.countAll();
        Assert.assertTrue(count == 2);
    }

    @Test
    public void createOrder() throws SQLException {
        Order order = new Order()
                .setNo("202000001")
                .setMemberId(3L)
                .setAmount(3.5f)
                .setQuantity(100.3f)
                .setSalesAt(Timestamp.valueOf("2020-05-01 09:30:00"));
        order.save(false);

        long count = Order.countAll();
        Assert.assertTrue(count == 1);
    }
}
