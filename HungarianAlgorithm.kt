package com.danale.edge.intelligence.body

import java.util.*
import kotlin.system.exitProcess

class HungarianAlgorithm(matrix: MutableList<List<Double>>) {
    var matrix: Array<DoubleArray>

    var squareInRow: IntArray
    var squareInCol: IntArray
    var rowIsCovered: IntArray
    var colIsCovered: IntArray
    var staredZeroesInRow: IntArray


    init {
        if (matrix.size != matrix[0].size) {
            try {
                throw IllegalAccessException("The matrix is not square!")
            } catch (ex: IllegalAccessException) {
                System.err.println(ex)
                exitProcess(1)
            }
        }

        this.matrix = Array(matrix.size) { i ->
            DoubleArray(matrix[i].size) { j -> matrix[i][j] }
        }

        squareInRow = IntArray(matrix.size)
        squareInCol = IntArray(matrix[0].size)
        rowIsCovered = IntArray(matrix.size)
        colIsCovered = IntArray(matrix[0].size)
        staredZeroesInRow = IntArray(matrix.size)

        Arrays.fill(staredZeroesInRow, -1)
        Arrays.fill(squareInRow, -1)
        Arrays.fill(squareInCol, -1)
    }

    /**
     * 找到最佳分配
     */
    fun findOptimalAssignment(): Array<IntArray?> {
        step1()
        step2()
        step3()

        while (!allColumnsAreCovered()) {
            var mainZero = step4()

            // while no zero found in step4
            while (mainZero == null) {
                step7()
                mainZero = step4()
            }

            if (squareInRow[mainZero[0]] == -1) {
                step6(mainZero)
                step3()
            } else {
                rowIsCovered[mainZero[0]] = 1
                colIsCovered[squareInRow[mainZero[0]]] = 0
                step7()
            }
        }

        val optimalAssignment = arrayOfNulls<IntArray>(matrix.size)
        for (i in squareInCol.indices) {
            optimalAssignment[i] = intArrayOf(i, squareInCol[i])
        }

        return optimalAssignment
    }

    private fun allColumnsAreCovered(): Boolean {
        for (i in colIsCovered) {
            if (i == 0) {
                return false
            }
        }
        return true
    }

    /**
     * 步骤1：
     * 减少矩阵，使得每一行和每一列中至少存在一个零：
     * 1.从行的每个元素中减去每行最小值
     * 2.从列的每个元素中减去每个列的最小值
     */
    private fun step1() {
        for (i in matrix.indices) {
            val currentRowMin = matrix[i].minOrNull() ?: 0.0
            for (j in matrix[i].indices) {
                matrix[i][j] -= currentRowMin
            }
        }

        for (i in matrix[0].indices) {
            val currentColMin = matrix.indices.minOf { matrix[it][i] }
            for (j in matrix.indices) {
                matrix[j][i] -= currentColMin
            }
        }
    }

    /**
     * 第2步：
     * 如果同一行或同一列中没有其他标记的零，则用“方块”标记每个 0
     */
    private fun step2() {
        val rowHasSquare = IntArray(matrix.size)
        val colHasSquare = IntArray(matrix[0].size)
        for (i in matrix.indices) {
            for (j in matrix.indices) {
                if (matrix[i][j] == 0.0 && rowHasSquare[i] == 0 && colHasSquare[j] == 0) {
                    rowHasSquare[i] = 1
                    colHasSquare[j] = 1
                    squareInRow[i] = j
                    squareInCol[j] = i
                    continue
                }
            }
        }
    }

    /**
     * 步骤3：
     * 覆盖所有标有“方块”的列
     */
    private fun step3() {
        for (i in squareInCol.indices) {
            colIsCovered[i] = if (squareInCol[i] != -1) 1 else 0
        }
    }

    /**
     * 步骤4：
     * 找到零值Z_0并将其标记为“0*”。
     *
     * @return Z_0在矩阵中的位置
     */
    private fun step4(): IntArray? {
        for (i in matrix.indices) {
            if (rowIsCovered[i] == 0) {
                for (j in matrix[i].indices) {
                    if (matrix[i][j] == 0.0 && colIsCovered[j] == 0) {
                        staredZeroesInRow[i] = j
                        return intArrayOf(i, j)
                    }
                }
            }
        }
        return null
    }

    /**
     * 第 6 步：
     * 创建一个由交替的“方块”和“0*”组成的链 K
     *
     * @param mainZero => 步骤 4 的 Z_0
     */
    private fun step6(mainZero: IntArray) {
        var i = mainZero[0]
        var j = mainZero[1]
        val K: MutableSet<IntArray> = LinkedHashSet()
        K.add(mainZero)
        var found = false
        do {
            found = if (squareInCol[j] != -1) {
                K.add(intArrayOf(squareInCol[j], j))
                true
            } else {
                false
            }

            if (!found) {
                break
            }

            i = squareInCol[j]
            j = staredZeroesInRow[i]
            found = if (j != -1) {
                K.add(intArrayOf(i, j))
                true
            } else {
                false
            }
        } while (found)

        for (zero in K) {
            if (squareInCol[zero[1]] == zero[0]) {
                squareInCol[zero[1]] = -1
                squareInRow[zero[0]] = -1
            }

            if (staredZeroesInRow[zero[0]] == zero[1]) {
                squareInRow[zero[0]] = zero[1]
                squareInCol[zero[1]] = zero[0]
            }
        }

        Arrays.fill(staredZeroesInRow, -1)
        Arrays.fill(rowIsCovered, 0)
        Arrays.fill(colIsCovered, 0)
    }

    /**
     *步骤7：
     * 1. 找到矩阵中最小的未覆盖值。
     * 2.从所有未覆盖的值中减去它
     * 3.将其添加到所有两次覆盖的值中
     */
    private fun step7() {
        var minUncoveredValue = Double.MAX_VALUE
        for (i in matrix.indices) {
            if (rowIsCovered[i] == 1) {
                continue
            }
            for (j in matrix[0].indices) {
                if (colIsCovered[j] == 0 && matrix[i][j] < minUncoveredValue) {
                    minUncoveredValue = matrix[i][j]
                }
            }
        }
        if (minUncoveredValue > 0) {
            for (i in matrix.indices) {
                for (j in matrix[0].indices) {
                    if (rowIsCovered[i] == 1 && colIsCovered[j] == 1) {
                        matrix[i][j] += minUncoveredValue
                    } else if (rowIsCovered[i] == 0 && colIsCovered[j] == 0) {
                        matrix[i][j] -= minUncoveredValue
                    }
                }
            }
        }
    }
}
