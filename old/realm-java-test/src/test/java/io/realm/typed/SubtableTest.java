/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import io.realm.test.EmployeesFixture;
import org.testng.annotations.Test;

import io.realm.test.TestEmployeeRow;
import io.realm.test.TestPhoneQuery;
import io.realm.test.TestPhoneRow;
import io.realm.test.TestPhoneTable;
import io.realm.test.TestPhoneView;

public class SubtableTest extends AbstractTest {

    @Test
    public void shouldSaveSubtableChanges() {
        TestEmployeeRow employee = employees.get(0);

        // check the basic operations
        TestPhoneTable phones1 = employee.getPhones();
        assertEquals(1, phones1.size());

        phones1.add("mobile", "111");
        assertEquals(2, phones1.size());

        TestPhoneTable phones2 = employee.getPhones();
        assertEquals(2, phones2.size());

        phones2.add("mobile", "222");
        assertEquals(3, phones2.size());

        phones2.insert(1, "home", "333");
        assertEquals(4, phones2.size());

        TestPhoneTable phones3 = employee.getPhones();
        assertEquals(2, phones3.type.equalTo("mobile").count());
        assertEquals(2, phones3.type.equalTo("home").count());

        assertEquals(1, phones3.number.equalTo("111").count());
        assertEquals(1, phones3.number.equalTo("123").count());
        assertEquals(0, phones3.number.equalTo("xxx").count());

        // check the search operations
        TestPhoneQuery phoneQuery = phones3.where().number.equalTo("111").number
                .notEqualTo("wrong").type.equalTo("mobile").type.notEqualTo("wrong");
        assertEquals(1, phoneQuery.count());

        TestPhoneView all = phoneQuery.findAll();
        assertEquals(1, all.size());
        checkPhone(all.get(0), "mobile", "111");

        checkPhone(phoneQuery.findFirst(), "mobile", "111");
        checkPhone(phoneQuery.findLast(), "mobile", "111");
        assertEquals(null, phoneQuery.findNext());

        // make sure the other sub-tables and independent and were not changed
        assertEquals(EmployeesFixture.PHONES[1].length, employees.get(1)
                .getPhones().size());
        assertEquals(EmployeesFixture.PHONES[2].length, employees.get(2)
                .getPhones().size());

        // check the clear operation on the query
        phoneQuery.clear();
        assertEquals(3, phones1.size());

        // check the clear operation
        phones3.clear();
        assertEquals(0, phones1.size());
        assertEquals(0, phones2.size());
        assertEquals(0, phones3.size());

        employees.clear();
    }

    private void checkPhone(TestPhoneRow phone, String type, String number) {
        assertEquals(type, phone.getType());
        assertEquals(number, phone.getNumber());
        assertEquals(type, phone.getType());
        assertEquals(number, phone.getNumber());
    }

    @Test
    public void shouldInvalidateWhenParentTableIsCleared() {
        TestEmployeeRow employee = employees.get(0);
        TestPhoneTable phones = employee.getPhones();
        assertTrue(phones.isValid());

        employees.clear();
        assertFalse(phones.isValid());
    }

    @Test
    public void shouldInvalidateOnRemovedRecordParentTable() {
        TestEmployeeRow employee = employees.get(0);
        TestPhoneTable phones = employee.getPhones();
        assertTrue(phones.isValid());

        employees.remove(2);
        assertTrue(phones.isValid());
    }

}
