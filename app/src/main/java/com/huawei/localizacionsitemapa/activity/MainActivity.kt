package com.huawei.localizacionsitemapa.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.huawei.hms.site.widget.SearchFilter
import com.huawei.hms.site.widget.SearchFragment
import com.huawei.hms.site.widget.SiteSelectionListener
import com.huawei.localizacionsitemapa.R
import com.huawei.localizacionsitemapa.adapter.SiteAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), SiteAdapter.SiteInterface {

    private var searchService: SearchService? = null
    private var fragment : SearchFragment? = null
    private var adapterSite : SiteAdapter? = null

    init {
        activityIdent = this
    }

    companion object {
        lateinit var activityIdent: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarBusqueda()
        bt_buscarDireccion.setOnClickListener {
            buscarLugares()
        }
    }

    private fun inicializarBusqueda() {
        // Para realizar la busqueda de lugares de Site Kit requerimos el Api key del archivo agconnect-services
        // debemos aplicar un encode al Api Key para evitar errores
        val encodedurl: String = URLEncoder.encode(getString(R.string.API_KEY_SITE), "UTF-8")
        searchService = SearchServiceFactory.create(baseContext, encodedurl)
        inicializarWidgetBusqueda(encodedurl)
    }

    private fun inicializarWidgetBusqueda(encodedurl: String) {
        // Para realizar la busqueda de lugares utilizando el fragmento predeterminado Site Kit
        // requerimos el Api key del archivo agconnect-services
        // debemos aplicar un encode al Api Key para evitar errores
        fragment = supportFragmentManager.findFragmentById(R.id.widget_fragment) as SearchFragment?
        fragment?.setApiKey(encodedurl)
        fragment?.setSearchFilter(obtenerOpcionesFiltro())
        fragment?.setOnSiteSelectedListener(object : SiteSelectionListener {
            override fun onSiteSelected(data: Site) {
                mostrarMensaje(data.getName())
            }

            override fun onError(status: SearchStatus) {
                mostrarMensaje("""${status.getErrorCode()}${status.getErrorMessage()}""".trimIndent())
            }
        })
    }

    fun inicializarAdaptador(lista: ArrayList<Site>) {
        adapterSite = SiteAdapter(baseContext, lista)
        adapterSite?.mOnClickListener = this
        rv_sites.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(baseContext)
            adapter = adapterSite
        }
    }

    private fun obtenerOpcionesFiltro(): SearchFilter {
        var search = SearchFilter()
        search.countryCode = "PE" // Peru (PE)
        search.location = Coordinate(-12.026389, -77.073655)
        search.poiType = obtenerType()
        return search
    }

    fun obtenerRequest() : QuerySuggestionRequest {
        val request = QuerySuggestionRequest()
        val location = Coordinate(-12.026389, -77.073655)
        request.query = et_buscador.text.toString()
        request.setLocation(location)
        request.setCountryCode("PE") // Peru (PE)
        request.poiTypes = obtenerType()
        return request
    }

    private fun obtenerType(): List<LocationType>? {
        val lista = ArrayList<LocationType>()
        lista.add(LocationType.CITIES)
        return lista
    }

    fun buscarLugares() {
        mostrarProgressBar()
        searchService?.querySuggestion(
            obtenerRequest(),
            object : SearchResultListener<QuerySuggestionResponse> {
                override fun onSearchResult(querySuggestionResponse: QuerySuggestionResponse?) {
                    ocultarProgressBar()
                    val siteList: List<Site>? = querySuggestionResponse?.getSites()
                    if (querySuggestionResponse == null || querySuggestionResponse.sites.isEmpty() || siteList.isNullOrEmpty()) {
                        mostrarMensaje("No hay lugares de busqueda")
                        return
                    }
                    inicializarAdaptador(siteList as ArrayList<Site>)
                }

                override fun onSearchError(searchStatus: SearchStatus) {
                    ocultarProgressBar()
                    mostrarMensaje("Error de Busqueda: " + searchStatus.errorCode)
                }
            })
    }

    override fun onClickSite(site: Site) {

    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(application, mensaje, Toast.LENGTH_LONG).show()
    }

    fun mostrarProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun ocultarProgressBar() {
        progressBar.visibility = View.GONE
    }

    fun goToGeocoder(view: View) {
    }
}