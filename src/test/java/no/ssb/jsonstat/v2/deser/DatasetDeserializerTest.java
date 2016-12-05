/**
 * Copyright (C) 2016 Hadrien Kohl (hadrien.kohl@gmail.com) and contributors
 *
 *     DatasetDeserializerTest.java
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
package no.ssb.jsonstat.v2.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.cartesianProduct;
import static java.util.Arrays.asList;

public class DatasetDeserializerTest {

    DatasetDeserializer ds = new DatasetDeserializer();

    private static List<String> join(List<List<String>> list) {
        return Lists.transform(list, Joiner.on("")::join);
    }

    private static List<String> concat(List<String>... lists) {
        return Lists.newArrayList(Iterables.concat(lists));
    }

    @DataProvider(name = "dates")
    public Object[][] getValidDates() {

        List<String> time = asList("T00:00", "T00:00:00");
        List<String> offset = asList("", "Z", "+00:00", "-00:00");
        List<String> dateTime = Lists.newArrayList(
                concat(
                        asList(""),
                        join(cartesianProduct(time, offset))
                )
        );

        List<String> formats = join(
                cartesianProduct(
                        asList("2000", "2000-01", "2000-01-01"),
                        Lists.newArrayList(dateTime))
        );

        return Lists.transform(formats, input -> Collections.singleton(input).toArray()).toArray(new Object[0][]);
    }


    @Test(dataProvider = "dates")
    public void testParseUpdated(String date) throws Exception {
        // Only check that we handle for now.
        ds.parseEcmaDate(date);
    }

    @Test
    public void testParseValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonParser mapParser = mapper.getFactory().createParser("" +
                "{ " +
                "  \"0\": 10," +
                "  \"1\": 20," +
                "  \"3\": 30," +
                "  \"4\": 40}"
        );
        mapParser.nextValue();

        JsonParser arrayParser = mapper.getFactory().createParser(
                "[ 10, 20, null, 30, 40 ]"
        );
        arrayParser.nextValue();

        List<Number> fromMap = ds.parseValues(mapParser, null);
        List<Number> fromArray = ds.parseValues(arrayParser, null);
        List<Number> expected = Lists.newArrayList(
                10, 20, null, 30, 40
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fromMap).as("deserialize values from map").isEqualTo(expected);
        softly.assertThat(fromArray).as("deserialize values from array").isEqualTo(expected);
        softly.assertAll();


    }
}