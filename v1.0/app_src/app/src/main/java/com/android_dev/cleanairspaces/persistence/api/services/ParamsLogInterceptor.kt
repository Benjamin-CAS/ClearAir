package com.android_dev.cleanairspaces.persistence.api.services

import android.util.Log
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.IOException
import kotlin.text.Charsets.UTF_8

/**
 * @author Benjamin
 * @description:
 * @date :2021.9.17 13:54
 */
class ParamsLogInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) return response
        Log.e(TAG, "intercept: ${response.isSuccessful}")
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val mediaType = response.body?.contentType()
        val content = response.body?.string()
        Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        Log.e(TAG, "请求地址: $request")
        request.body?.let { printParams(it) }
        Log.e(TAG, "请求状态码: ${response.code}")
        Log.e(TAG, "返回结果: $content")
        Log.e(TAG, "请求耗时: $duration")
        Log.e(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        return response.newBuilder().body(content?.toResponseBody(mediaType)).build()
    }

    private fun printParams(body:RequestBody){
        val buffer = Buffer()
        try {
            body.writeTo(buffer)
            val contentType = body.contentType()
            val charset = contentType?.charset(UTF_8)
            val params = charset?.let { buffer.readString(it) }
            Log.e(TAG, "请求参数： $params")
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
    companion object{
        const val TAG = "ParamsLogInterceptor"
    }
}