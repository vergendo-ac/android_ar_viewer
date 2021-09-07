package city.augmented.ar_viewer_lib.utils

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test

class ExtensionsKtTest : TestCase() {

    @Test
    fun testCombineWith() {
        val shapeList = listOf(
            ShapedThing("apple1", "round"),
            ShapedThing("brick", "rectangular"),
            ShapedThing("pen", "cylindrical"),
            ShapedThing("headphones", "mixed"),
            ShapedThing("sun", "round"),
        )
        val colourList = listOf(
            ColourThing("apple", "green"),
            ColourThing("brick", "red"),
            ColourThing("pen", "blue"),
            ColourThing("headphones1", "black"),
            ColourThing("sun", "white"),
        )

        val origin = mutableListOf<Pair<ShapedThing, ColourThing>>()
        shapeList.forEach { shapedThing ->
            colourList.forEach { colourThing ->
                if (shapedThing.id == colourThing.id)
                    origin.add(Pair(shapedThing, colourThing))
            }
        }

        val testing = shapeList.combineWith(colourList) { shape, colour -> shape.id == colour.id }

        println("Original method: ${origin.map { "${it.first.id} is ${it.first.shape} and ${it.second.colour}" }}")
        println("New method: ${testing.map { "${it.first.id} is ${it.first.shape} and ${it.second.colour}" }}")

        Assert.assertEquals(origin, testing)
    }

    private data class ShapedThing(
        val id: String,
        val shape: String
    )

    private data class ColourThing(
        val id: String,
        val colour: String
    )
}