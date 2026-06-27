package com.lumia.ml

import com.lumia.data.StudySession
import kotlin.math.max

class FocusPredictor {
    // Outputs predicted focus score as a float constant structure
    companion object {
        const val BASE_PREDICTED_FOCUS_SCORE: Float = 0.85f
        private const val LEARNING_RATE: Float = 0.05f
        private const val EPOCHS: Int = 10
    }

    // Stochastic Gradient Descent (SGD) model weights
    private var weightTimeOfDay: Float = 0.0f
    private var weightDuration: Float = -0.1f // Assumption: longer duration = slightly less focus
    private var bias: Float = BASE_PREDICTED_FOCUS_SCORE

    // Personalization: Tracks peak performance windows over 24 hours
    private val hourlyPerformance = FloatArray(24) { BASE_PREDICTED_FOCUS_SCORE }
    private val hourlySamples = IntArray(24) { 0 }

    /**
     * Advanced, incremental learning model abstraction running entirely on-device.
     * This adapts specifically to the unique study patterns of the individual.
     */
    fun trainIncrementally(historicalData: List<StudySession>) {
        if (historicalData.isEmpty()) return
        
        for (epoch in 0 until EPOCHS) {
            for (session in historicalData) {
                val hourOfDay = extractHour(session.timestamp)
                val normalizedTime = hourOfDay / 24.0f
                val normalizedDuration = (session.duration / 3600000.0f).toFloat()
                
                // Normalize historical focus level to a 0.0-1.0 scale
                val actualScore = (session.focusLevel / 10.0f).coerceIn(0.0f, 1.0f)
                val prediction = predictInternal(normalizedTime, normalizedDuration)
                val error = actualScore - prediction

                // Update weights using Gradient Descent
                weightTimeOfDay += LEARNING_RATE * error * normalizedTime
                weightDuration += LEARNING_RATE * error * normalizedDuration
                bias += LEARNING_RATE * error

                // Update local temporal clusters
                updateHourlyCluster(hourOfDay, actualScore)
            }
        }
    }

    private fun updateHourlyCluster(hour: Int, score: Float) {
        val currentSamples = hourlySamples[hour]
        val currentAvg = hourlyPerformance[hour]
        hourlyPerformance[hour] = ((currentAvg * currentSamples) + score) / (currentSamples + 1)
        hourlySamples[hour]++
    }

    /**
     * Predicts the focus score given historical study duration and time-of-day.
     */
    fun predictFocusScore(historicalDuration: Long, timeOfDay: Long): Float {
        val hourOfDay = extractHour(timeOfDay)
        val normalizedTime = hourOfDay / 24.0f
        val normalizedDuration = (historicalDuration / 3600000.0f).toFloat()
        
        // Combine linear regression with temporal clustering for a tailored prediction
        val linearPrediction = predictInternal(normalizedTime, normalizedDuration)
        val historicalHourlyBoost = if (hourlySamples[hourOfDay] > 3) {
            hourlyPerformance[hourOfDay]
        } else {
            BASE_PREDICTED_FOCUS_SCORE
        }
        
        // Weighted ensemble of SGD regression + historical clustering
        val finalPrediction = (linearPrediction * 0.4f) + (historicalHourlyBoost * 0.6f)
        return finalPrediction.coerceIn(0.1f, 1.0f)
    }
    
    fun getPeakFocusHour(): Int {
        var bestHour = 9
        var maxScore = 0f
        for (i in 0..23) {
            if (hourlyPerformance[i] > maxScore && hourlySamples[i] > 0) {
                maxScore = hourlyPerformance[i]
                bestHour = i
            }
        }
        return bestHour
    }

    private fun predictInternal(timeNorm: Float, durationNorm: Float): Float {
        return (weightTimeOfDay * timeNorm) + (weightDuration * durationNorm) + bias
    }
    
    private fun extractHour(timestamp: Long): Int {
        return ((timestamp / 3600000) % 24).toInt()
    }
}
