# /chart/templates/)helpers.tpl
{{/*
Generate a fullname for the release
*/}}
{{- define "petclinic.fullname" -}}
{{- printf "%s" .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
Just return the name of the app (release)
*/}}
{{- define "petclinic.name" -}}
{{ .Release.Name }}
{{- end }}

