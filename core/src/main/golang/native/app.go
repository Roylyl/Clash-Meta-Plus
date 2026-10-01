package main

//#include "bridge.h"
import "C"

import (
	"errors"
	"unsafe"

	"cfa/native/app"

	"github.com/metacubex/mihomo/log"
	"github.com/metacubex/mihomo/hub/executor"
)

func openRemoteContent(url string) (int, error) {
	u := C.CString(url)
	e := (*C.char)(C.malloc(1024))

	log.Debugln("Open remote url: %s", url)

	defer C.free(unsafe.Pointer(e))

	fd := C.open_content(u, e, 1024)

	if fd < 0 {
		return -1, errors.New(C.GoString(e))
	}

	return int(fd), nil
}

//export notifyDnsChanged
func notifyDnsChanged(dnsList C.c_string) {
	d := C.GoString(dnsList)

	app.NotifyDnsChanged(d)
}

//export notifyInstalledAppsChanged
func notifyInstalledAppsChanged(uids C.c_string) {
	u := C.GoString(uids)

	app.NotifyInstallAppsChanged(u)
}

//export notifyTimeZoneChanged
func notifyTimeZoneChanged(name C.c_string, offset C.int) {
	app.NotifyTimeZoneChanged(C.GoString(name), int(offset))
}


//export queryConfiguration
func queryConfiguration() *C.char {
	general := executor.GetGeneral()
	response := &struct {
		MixedPort int `json:"mixed-port"`
		HttpPort int `json:"port"`
		SocksPort int `json:"socks-port"`
		AllowLan bool `json:"allow-lan"`
		BindAddress string `json:"bind-address"`
	}{general.MixedPort, general.Port, general.SocksPort, general.AllowLan, general.BindAddress}

	return marshalJson(&response)
}

func init() {
	app.ApplyContentContext(openRemoteContent)
}