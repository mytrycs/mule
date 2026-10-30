#!/usr/bin/env python3
"""Assemble the native runtime from the renamed source build; see --help."""
from pathlib import Path
import runpy
runpy.run_path(str(Path(__file__).with_name('assemble-native.py')), run_name='__main__')
