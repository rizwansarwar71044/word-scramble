package com.example

import com.example.data.WordEntry
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun wordEntryList_isCorrectOptionSize() {
    val words = WordEntry.ALL_WORDS
    assertEquals("Should generate exactly 1000 words for the levels", 1000, words.size)
    
    // Ensure words are valid uppercase A-Z only and length fits 2..12
    for (entry in words) {
        assertTrue("Word ${entry.word} must be uppercase A-Z only", entry.word.matches(Regex("[A-Z]+")))
        assertTrue("Word ${entry.word} length should be between 2 and 12", entry.word.length in 2..12)
    }

    // Verify all words are unique
    val uniqueWords = words.map { it.word }.toSet()
    assertEquals("All word values must be unique to avoid duplicate puzzles", 1000, uniqueWords.size)
  }
}
