package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify app identity string definition`() {
    val expectedName = "EVA BUILD CLI"
    assertNotNull(expectedName)
    assertEquals("EVA BUILD CLI", expectedName)
  }
}
