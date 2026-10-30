#!/usr/bin/env python3
"""Resize only the named VS Code window on an explicit private X11 test display.

Electron's debugger omits Browser.setWindowBounds. This uses the real X11
window, not browser viewport emulation or screenshot cropping.
"""
import ctypes as c
import json
import os
import sys

width, height = map(int, sys.argv[1:3])
if not 800 <= width <= 3840 or not 600 <= height <= 2160:
    raise SystemExit('Bounded review sizes only')
display_name = os.environ.get('DISPLAY')
if not display_name:
    raise SystemExit('Set DISPLAY to the isolated test display')
lib = c.CDLL('libX11.so.6')
lib.XOpenDisplay.argtypes = [c.c_char_p]; lib.XOpenDisplay.restype = c.c_void_p
lib.XDefaultRootWindow.argtypes = [c.c_void_p]; lib.XDefaultRootWindow.restype = c.c_ulong
lib.XQueryTree.argtypes = [c.c_void_p, c.c_ulong, c.POINTER(c.c_ulong), c.POINTER(c.c_ulong), c.POINTER(c.POINTER(c.c_ulong)), c.POINTER(c.c_uint)]
lib.XFetchName.argtypes = [c.c_void_p, c.c_ulong, c.POINTER(c.c_char_p)]
lib.XInternAtom.argtypes = [c.c_void_p, c.c_char_p, c.c_int]; lib.XInternAtom.restype = c.c_ulong
lib.XGetWindowProperty.argtypes = [c.c_void_p, c.c_ulong, c.c_ulong, c.c_long, c.c_long, c.c_int, c.c_ulong, c.POINTER(c.c_ulong), c.POINTER(c.c_int), c.POINTER(c.c_ulong), c.POINTER(c.c_ulong), c.POINTER(c.c_void_p)]
lib.XMoveResizeWindow.argtypes = [c.c_void_p, c.c_ulong, c.c_int, c.c_int, c.c_uint, c.c_uint]
lib.XSync.argtypes = [c.c_void_p, c.c_int]
lib.XFree.argtypes = [c.c_void_p]
lib.XCloseDisplay.argtypes = [c.c_void_p]
d = lib.XOpenDisplay(display_name.encode())
if not d: raise SystemExit('Cannot open the isolated X11 display')
root = lib.XDefaultRootWindow(d)
returned_root, parent = c.c_ulong(), c.c_ulong()
children = c.POINTER(c.c_ulong)(); count = c.c_uint()
lib.XQueryTree(d, root, c.byref(returned_root), c.byref(parent), c.byref(children), c.byref(count))
matches = []
for i in range(count.value):
    atom = lib.XInternAtom(d, b'_NET_WM_NAME', 0)
    actual_type, size, after = c.c_ulong(), c.c_ulong(), c.c_ulong()
    fmt, data = c.c_int(), c.c_void_p()
    lib.XGetWindowProperty(d, children[i], atom, 0, 2048, 0, 0, c.byref(actual_type), c.byref(fmt), c.byref(size), c.byref(after), c.byref(data))
    title = c.string_at(data, size.value).decode(errors='replace') if data.value and fmt.value == 8 else ''
    if data.value: lib.XFree(data)
    if not title:
        name = c.c_char_p()
        if lib.XFetchName(d, children[i], c.byref(name)) and name.value:
            title = name.value.decode(errors='replace'); lib.XFree(name)
    if 'ZERNOVA Studio' in title: matches.append((children[i], title))
if count.value: lib.XFree(children)
if len(matches) != 1:
    lib.XCloseDisplay(d)
    raise SystemExit(f'Expected exactly one ZERNOVA Studio review window, found {matches!r}')
window, title = matches[0]
lib.XMoveResizeWindow(d, window, 0, 0, width, height)
lib.XSync(d, 0); lib.XCloseDisplay(d)
print(json.dumps({'windowId': window, 'title': title, 'width': width, 'height': height, 'display': display_name, 'method': 'XMoveResizeWindow on isolated native window'}))
