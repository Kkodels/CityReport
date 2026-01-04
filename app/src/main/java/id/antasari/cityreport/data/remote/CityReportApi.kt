package id.antasari.cityreport.data.remote

import id.antasari.cityreport.data.remote.dto.CreateReportRequest
import id.antasari.cityreport.data.remote.dto.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CityReportApi {

    @GET("api/reports")
    suspend fun getReports(): Response<List<ReportResponse>>

    @POST("api/reports")
    suspend fun createReport(
        @Body body: CreateReportRequest
    ): Response<ReportResponse>   // sesuaikan dengan backend, kalau backend balas list / map ubah tipe ini
}
