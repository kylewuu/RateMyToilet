package com.example.ratemytoilet

import android.content.Context
import com.example.ratemytoilet.database.ReviewCard
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * Renderer for the markers clusters on the map
 */
class MarkerClusterRenderer (context:Context, map: GoogleMap, clusterManager: ClusterManager<ReviewCard>?) : DefaultClusterRenderer<ReviewCard> (context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: ReviewCard, markerOptions: MarkerOptions) {
        if (item != null && markerOptions != null) {
            super.onBeforeClusterItemRendered(item, markerOptions)
        }
        markerOptions?.icon(item?.getIcon())
        markerOptions?.title(item?.title)
        markerOptions?.snippet(item?.snippet)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<ReviewCard>): Boolean {
        return cluster.size > 4
    }
}
