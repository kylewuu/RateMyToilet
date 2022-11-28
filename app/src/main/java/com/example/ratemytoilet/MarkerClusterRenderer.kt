package com.example.ratemytoilet

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class MarkerClusterRenderer (context:Context, map: GoogleMap, clusterManager: ClusterManager<MyItem>?) : DefaultClusterRenderer<MyItem> (context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
        if (item != null && markerOptions != null) {
            super.onBeforeClusterItemRendered(item, markerOptions)
        }
        markerOptions?.icon(item?.getIcon())
        markerOptions?.title(item?.title)
        markerOptions?.snippet(item?.snippet)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<MyItem>): Boolean {
        return cluster.size > 4
    }
}