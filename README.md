# Vert.x Zip Module

Simple worker module that given the file path of a file or directory, zip it returns the filename in a Json message

## Configuration

Fields:

`address`: The address on the event bus where to listen for messages

## Usage

Send a Json message to `address`.

Fields:

* `path`: Mandatory. Path of a file or directory to zip. It must exist
* `zipFile`: Optional. Name of the output zip file. If not specified a ramdom name will be generated. The zip file will be created if it does not exist already.
* `deletePath`: Optional. Boolean. If `true` then the source path will be deleted afterwards. Default is `false`.
