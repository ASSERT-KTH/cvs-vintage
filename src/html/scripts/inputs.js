// Handling for automatic form submittal via a drop-down value change.
function submitFormTo(form, name) {
  var input = document.createElement('input');
  input.type = 'hidden';
  input.name = name;
  form.appendChild(input);
  form.submit();
}
