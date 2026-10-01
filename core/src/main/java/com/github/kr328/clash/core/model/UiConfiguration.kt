package com.github.kr328.clash.core.model

import android.os.Parcel
import android.os.Parcelable
import com.github.kr328.clash.core.util.Parcelizer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
class UiConfiguration(
    @SerialName("mixed-port") val mixedPort: Int = 0,
    @SerialName("port") val httpPort: Int = 0,
    @SerialName("socks-port") val socksPort: Int = 0,
    @SerialName("allow-lan") val allowLan: Boolean = false,
    @SerialName("bind-address") val bindAddress: String = "",
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        Parcelizer.encodeToParcel(serializer(), parcel, this)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UiConfiguration> {
        override fun createFromParcel(parcel: Parcel): UiConfiguration {
            return Parcelizer.decodeFromParcel(serializer(), parcel)
        }

        override fun newArray(size: Int): Array<UiConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}
