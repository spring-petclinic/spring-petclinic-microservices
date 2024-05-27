import {
  SelectableContext_default,
  dataAttr,
  makeEventKey,
  useEventCallback
} from "./chunk-KHD3S7TC.js";
import {
  require_classnames,
  useBootstrapPrefix
} from "./chunk-MNC46KYS.js";
import {
  require_jsx_runtime
} from "./chunk-ZF6AKY5B.js";
import {
  __toESM,
  require_react
} from "./chunk-6EPYQA3G.js";

// node_modules/react-bootstrap/esm/NavLink.js
var import_classnames = __toESM(require_classnames());
var React6 = __toESM(require_react());

// node_modules/@restart/ui/esm/Anchor.js
var React2 = __toESM(require_react());

// node_modules/@restart/hooks/esm/useCallbackRef.js
var import_react = __toESM(require_react());
function useCallbackRef() {
  return (0, import_react.useState)(null);
}

// node_modules/@restart/hooks/esm/useEventListener.js
var import_react2 = __toESM(require_react());
function useEventListener(eventTarget, event, listener, capture = false) {
  const handler = useEventCallback(listener);
  (0, import_react2.useEffect)(() => {
    const target = typeof eventTarget === "function" ? eventTarget() : eventTarget;
    target.addEventListener(event, handler, capture);
    return () => target.removeEventListener(event, handler, capture);
  }, [eventTarget]);
}

// node_modules/@restart/hooks/esm/useGlobalListener.js
var import_react3 = __toESM(require_react());

// node_modules/@restart/hooks/esm/useInterval.js
var import_react4 = __toESM(require_react());

// node_modules/@restart/hooks/esm/useRafInterval.js
var import_react5 = __toESM(require_react());

// node_modules/@restart/hooks/esm/useMergeState.js
var import_react6 = __toESM(require_react());

// node_modules/@restart/hooks/esm/useImage.js
var import_react7 = __toESM(require_react());

// node_modules/@restart/hooks/esm/useResizeObserver.js
var import_react8 = __toESM(require_react());

// node_modules/@restart/ui/esm/Button.js
var React = __toESM(require_react());
var import_jsx_runtime = __toESM(require_jsx_runtime());
var _excluded = ["as", "disabled"];
function _objectWithoutPropertiesLoose(source, excluded) {
  if (source == null)
    return {};
  var target = {};
  var sourceKeys = Object.keys(source);
  var key, i;
  for (i = 0; i < sourceKeys.length; i++) {
    key = sourceKeys[i];
    if (excluded.indexOf(key) >= 0)
      continue;
    target[key] = source[key];
  }
  return target;
}
function isTrivialHref(href) {
  return !href || href.trim() === "#";
}
function useButtonProps({
  tagName,
  disabled,
  href,
  target,
  rel,
  role,
  onClick,
  tabIndex = 0,
  type
}) {
  if (!tagName) {
    if (href != null || target != null || rel != null) {
      tagName = "a";
    } else {
      tagName = "button";
    }
  }
  const meta = {
    tagName
  };
  if (tagName === "button") {
    return [{
      type: type || "button",
      disabled
    }, meta];
  }
  const handleClick = (event) => {
    if (disabled || tagName === "a" && isTrivialHref(href)) {
      event.preventDefault();
    }
    if (disabled) {
      event.stopPropagation();
      return;
    }
    onClick == null ? void 0 : onClick(event);
  };
  const handleKeyDown = (event) => {
    if (event.key === " ") {
      event.preventDefault();
      handleClick(event);
    }
  };
  if (tagName === "a") {
    href || (href = "#");
    if (disabled) {
      href = void 0;
    }
  }
  return [{
    role: role != null ? role : "button",
    // explicitly undefined so that it overrides the props disabled in a spread
    // e.g. <Tag {...props} {...hookProps} />
    disabled: void 0,
    tabIndex: disabled ? void 0 : tabIndex,
    href,
    target: tagName === "a" ? target : void 0,
    "aria-disabled": !disabled ? void 0 : disabled,
    rel: tagName === "a" ? rel : void 0,
    onClick: handleClick,
    onKeyDown: handleKeyDown
  }, meta];
}
var Button = React.forwardRef((_ref, ref) => {
  let {
    as: asProp,
    disabled
  } = _ref, props = _objectWithoutPropertiesLoose(_ref, _excluded);
  const [buttonProps, {
    tagName: Component
  }] = useButtonProps(Object.assign({
    tagName: asProp,
    disabled
  }, props));
  return (0, import_jsx_runtime.jsx)(Component, Object.assign({}, props, buttonProps, {
    ref
  }));
});
Button.displayName = "Button";
var Button_default = Button;

// node_modules/@restart/ui/esm/Anchor.js
var import_jsx_runtime2 = __toESM(require_jsx_runtime());
var _excluded2 = ["onKeyDown"];
function _objectWithoutPropertiesLoose2(source, excluded) {
  if (source == null)
    return {};
  var target = {};
  var sourceKeys = Object.keys(source);
  var key, i;
  for (i = 0; i < sourceKeys.length; i++) {
    key = sourceKeys[i];
    if (excluded.indexOf(key) >= 0)
      continue;
    target[key] = source[key];
  }
  return target;
}
function isTrivialHref2(href) {
  return !href || href.trim() === "#";
}
var Anchor = React2.forwardRef((_ref, ref) => {
  let {
    onKeyDown
  } = _ref, props = _objectWithoutPropertiesLoose2(_ref, _excluded2);
  const [buttonProps] = useButtonProps(Object.assign({
    tagName: "a"
  }, props));
  const handleKeyDown = useEventCallback((e) => {
    buttonProps.onKeyDown(e);
    onKeyDown == null ? void 0 : onKeyDown(e);
  });
  if (isTrivialHref2(props.href) || props.role === "button") {
    return (0, import_jsx_runtime2.jsx)("a", Object.assign({
      ref
    }, props, buttonProps, {
      onKeyDown: handleKeyDown
    }));
  }
  return (0, import_jsx_runtime2.jsx)("a", Object.assign({
    ref
  }, props, {
    onKeyDown
  }));
});
Anchor.displayName = "Anchor";
var Anchor_default = Anchor;

// node_modules/@restart/ui/esm/NavItem.js
var React5 = __toESM(require_react());
var import_react9 = __toESM(require_react());

// node_modules/@restart/ui/esm/NavContext.js
var React3 = __toESM(require_react());
var NavContext = React3.createContext(null);
NavContext.displayName = "NavContext";
var NavContext_default = NavContext;

// node_modules/@restart/ui/esm/TabContext.js
var React4 = __toESM(require_react());
var TabContext = React4.createContext(null);
var TabContext_default = TabContext;

// node_modules/@restart/ui/esm/NavItem.js
var import_jsx_runtime3 = __toESM(require_jsx_runtime());
var _excluded3 = ["as", "active", "eventKey"];
function _objectWithoutPropertiesLoose3(source, excluded) {
  if (source == null)
    return {};
  var target = {};
  var sourceKeys = Object.keys(source);
  var key, i;
  for (i = 0; i < sourceKeys.length; i++) {
    key = sourceKeys[i];
    if (excluded.indexOf(key) >= 0)
      continue;
    target[key] = source[key];
  }
  return target;
}
function useNavItem({
  key,
  onClick,
  active,
  id,
  role,
  disabled
}) {
  const parentOnSelect = (0, import_react9.useContext)(SelectableContext_default);
  const navContext = (0, import_react9.useContext)(NavContext_default);
  const tabContext = (0, import_react9.useContext)(TabContext_default);
  let isActive = active;
  const props = {
    role
  };
  if (navContext) {
    if (!role && navContext.role === "tablist")
      props.role = "tab";
    const contextControllerId = navContext.getControllerId(key != null ? key : null);
    const contextControlledId = navContext.getControlledId(key != null ? key : null);
    props[dataAttr("event-key")] = key;
    props.id = contextControllerId || id;
    isActive = active == null && key != null ? navContext.activeKey === key : active;
    if (isActive || !(tabContext != null && tabContext.unmountOnExit) && !(tabContext != null && tabContext.mountOnEnter))
      props["aria-controls"] = contextControlledId;
  }
  if (props.role === "tab") {
    props["aria-selected"] = isActive;
    if (!isActive) {
      props.tabIndex = -1;
    }
    if (disabled) {
      props.tabIndex = -1;
      props["aria-disabled"] = true;
    }
  }
  props.onClick = useEventCallback((e) => {
    if (disabled)
      return;
    onClick == null ? void 0 : onClick(e);
    if (key == null) {
      return;
    }
    if (parentOnSelect && !e.isPropagationStopped()) {
      parentOnSelect(key, e);
    }
  });
  return [props, {
    isActive
  }];
}
var NavItem = React5.forwardRef((_ref, ref) => {
  let {
    as: Component = Button_default,
    active,
    eventKey
  } = _ref, options = _objectWithoutPropertiesLoose3(_ref, _excluded3);
  const [props, meta] = useNavItem(Object.assign({
    key: makeEventKey(eventKey, options.href),
    active
  }, options));
  props[dataAttr("active")] = meta.isActive;
  return (0, import_jsx_runtime3.jsx)(Component, Object.assign({}, options, props, {
    ref
  }));
});
NavItem.displayName = "NavItem";
var NavItem_default = NavItem;

// node_modules/react-bootstrap/esm/NavLink.js
var import_jsx_runtime4 = __toESM(require_jsx_runtime());
var NavLink = React6.forwardRef(({
  bsPrefix,
  className,
  as: Component = Anchor_default,
  active,
  eventKey,
  disabled = false,
  ...props
}, ref) => {
  bsPrefix = useBootstrapPrefix(bsPrefix, "nav-link");
  const [navItemProps, meta] = useNavItem({
    key: makeEventKey(eventKey, props.href),
    active,
    disabled,
    ...props
  });
  return (0, import_jsx_runtime4.jsx)(Component, {
    ...props,
    ...navItemProps,
    ref,
    disabled,
    className: (0, import_classnames.default)(className, bsPrefix, disabled && "disabled", meta.isActive && "active")
  });
});
NavLink.displayName = "NavLink";
var NavLink_default = NavLink;

// node_modules/@restart/hooks/esm/useForceUpdate.js
var import_react10 = __toESM(require_react());
function useForceUpdate() {
  const [, dispatch] = (0, import_react10.useReducer)((state) => !state, false);
  return dispatch;
}

export {
  useCallbackRef,
  useEventListener,
  useButtonProps,
  Button_default,
  Anchor_default,
  useForceUpdate,
  NavContext_default,
  TabContext_default,
  useNavItem,
  NavItem_default,
  NavLink_default
};
//# sourceMappingURL=chunk-S37JW5GH.js.map
